package com.example.projecttodo.ThongKe;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * StatisticsUpdater – chỉ cập nhật dữ liệu đã tính toán
 * vào các trường sẵn có trong:
 *
 * users/{uid}/statistics/all
 * users/{uid}/statistics/last7Days
 * users/{uid}/statistics/last30Days
 *
 * Mỗi nhánh gồm: completed, overdue, incomplete, total
 *
 * Quy ước:
 *  - all.*         : tất cả task
 *  - last7Days.*   : task có createdAt/createAt trong [now - 7 ngày, now]
 *  - last30Days.*  : task có createdAt/createAt trong [now - 30 ngày, now]
 *  - completed     : completed == true
 *  - overdue       : completed == false && deadline < now
 *  - incomplete    : completed == false && (deadline không có || deadline >= now)
 *
 * Không tạo mới cấu trúc – chỉ ghi số liệu.
 */
public class StatisticsUpdater {

    private static final String TAG = "StatisticsUpdater";

    private final DatabaseReference userRef;

    // Định dạng ngày cho cả createdAt / deadline: "HH:mm dd/MM/yyyy"
    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    public StatisticsUpdater(String userId) {
        this.userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId);
    }

    // Bộ đếm
    private static class Counter {
        int completed = 0;
        int overdue = 0;
        int incomplete = 0;
        int total = 0;
    }

    /**
     * Hàm chính để tính toán và update thống kê
     */
    public void updateStatistics(OnCompleteListener<Void> listener) {

        userRef.child("tasks")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        Counter all = new Counter();
                        Counter last7 = new Counter();
                        Counter last30 = new Counter();

                        long now = System.currentTimeMillis();

                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_YEAR, -7);
                        long last7Start = cal.getTimeInMillis();

                        cal = Calendar.getInstance();
                        cal.add(Calendar.DAY_OF_YEAR, -30);
                        long last30Start = cal.getTimeInMillis();

                        // DUYỆT DANH SÁCH TASK
                        for (DataSnapshot taskSnap : snapshot.getChildren()) {

                            Boolean completedVal =
                                    taskSnap.child("completed").getValue(Boolean.class);

                            String deadlineStr =
                                    taskSnap.child("deadline").getValue(String.class);

                            // createdAt có thể là "createdAt" hoặc "createAt"
                            String createdAtStr = taskSnap.child("createdAt").getValue(String.class);
                            if (createdAtStr == null) {
                                createdAtStr = taskSnap.child("createAt").getValue(String.class);
                            }

                            boolean isCompleted = Boolean.TRUE.equals(completedVal);
                            long deadlineMs = parseDate(deadlineStr);
                            long createdAtMs = parseDate(createdAtStr);

                            // ====== TÍNH TOÁN CHO TẬP ALL ======
                            all.total++;
                            if (isCompleted) {
                                all.completed++;
                            } else {
                                if (deadlineMs > 0 && deadlineMs < now) {
                                    all.overdue++;
                                } else {
                                    // chưa hoàn thành và chưa quá hạn (hoặc không có deadline)
                                    all.incomplete++;
                                }
                            }

                            // ====== LAST 7 DAYS (dựa vào createdAt) ======
                            if (createdAtMs > 0 &&
                                    createdAtMs >= last7Start && createdAtMs <= now) {

                                last7.total++;
                                if (isCompleted) {
                                    last7.completed++;
                                } else {
                                    if (deadlineMs > 0 && deadlineMs < now) {
                                        last7.overdue++;
                                    } else {
                                        last7.incomplete++;
                                    }
                                }
                            }

                            // ====== LAST 30 DAYS (dựa vào createdAt) ======
                            if (createdAtMs > 0 &&
                                    createdAtMs >= last30Start && createdAtMs <= now) {

                                last30.total++;
                                if (isCompleted) {
                                    last30.completed++;
                                } else {
                                    if (deadlineMs > 0 && deadlineMs < now) {
                                        last30.overdue++;
                                    } else {
                                        last30.incomplete++;
                                    }
                                }
                            }
                        }

                        // Chuẩn bị dữ liệu để update (chỉ ghi số, không đổi cấu trúc)
                        Map<String, Object> updates = new HashMap<>();

                        // ALL
                        updates.put("statistics/all/completed", all.completed);
                        updates.put("statistics/all/overdue", all.overdue);
                        updates.put("statistics/all/incomplete", all.incomplete);
                        updates.put("statistics/all/total", all.total);

                        // LAST 7 DAYS
                        updates.put("statistics/last7Days/completed", last7.completed);
                        updates.put("statistics/last7Days/overdue", last7.overdue);
                        updates.put("statistics/last7Days/incomplete", last7.incomplete);
                        updates.put("statistics/last7Days/total", last7.total);

                        // LAST 30 DAYS
                        updates.put("statistics/last30Days/completed", last30.completed);
                        updates.put("statistics/last30Days/overdue", last30.overdue);
                        updates.put("statistics/last30Days/incomplete", last30.incomplete);
                        updates.put("statistics/last30Days/total", last30.total);

                        // Ghi vào Firebase
                        userRef.updateChildren(updates)
                                .addOnCompleteListener(listener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "updateStatistics cancelled", error.toException());
                        if (listener != null) {
                            listener.onComplete(
                                    Tasks.forException(error.toException())
                            );
                        }
                    }
                });
    }

    /**
     * Chuyển String ngày (deadline / createdAt) → millis
     */
    private long parseDate(String s) {
        if (s == null || s.trim().isEmpty()) return -1;
        try {
            return dateFormat.parse(s).getTime();
        } catch (ParseException e) {
            Log.w(TAG, "Lỗi parse date: " + s, e);
            return -1;
        }
    }
}
