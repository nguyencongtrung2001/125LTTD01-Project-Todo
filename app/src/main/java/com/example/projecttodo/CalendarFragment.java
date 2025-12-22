package com.example.projecttodo;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.HenGio.ReminderReceiver;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;

    private RecyclerView rvCalendarTasks;

    private String selectedDate = null;

    private String userId = "";

    private final List<String> reminderList = new ArrayList<>();
    private ReminderAdapter reminderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setVietnameseLocale(requireContext());
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        SharedPreferences sp = requireContext().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        userId = sp.getString("user_id", "OeDQ20MOzT2NnC-Tp31"); // fallback nếu chưa login

        initViews(view);
        return view;
    }

    private void initViews(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        calendarView.setDate(System.currentTimeMillis(), false, true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        loadRemindersForDate();

        rvCalendarTasks = view.findViewById(R.id.rvCalendarTasks);
        ImageButton btnAdd = view.findViewById(R.id.btnAdd);

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderAdapter = new ReminderAdapter(reminderList);
        rvCalendarTasks.setAdapter(reminderAdapter);

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderAdapter = new ReminderAdapter(reminderList);
        rvCalendarTasks.setAdapter(reminderAdapter);
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
            Toast.makeText(getContext(), "Ngày đã chọn: " + selectedDate, Toast.LENGTH_SHORT).show();
            loadRemindersForDate(); });

        btnAdd.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(getContext(), "Vui lòng chọn ngày trong lịch!", Toast.LENGTH_SHORT).show();
                return;
            }
            PopupMenu popup = new PopupMenu(getContext(), btnAdd);
            popup.getMenu().add("Tạo nhắc hẹn");
            popup.getMenu().add("Thêm công việc");
            popup.getMenu().add("Sinh nhật");

            popup.setOnMenuItemClickListener(item -> {
                String choice = item.getTitle().toString();
                if (choice.equals("Tạo nhắc hẹn")) showReminderDialog();
                if (choice.equals("Thêm công việc")) {
                    Intent intent = new Intent(getContext(), AddTaskActivity.class);
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                }
                if (choice.equals("Sinh nhật")) showBirthdayDialog();
                return true;
            });
            popup.show();
        });
    }

    private void showBirthdayDialog() {
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_set_reminder);

        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        TextView tvSelectedDate = dialog.findViewById(R.id.tvSelectedDate);
        Button btnCancel = dialog.findViewById(R.id.btnCancelReminder);
        Button btnCreate = dialog.findViewById(R.id.btnCreateReminder);

        tvSelectedDate.setText("Sinh nhật: " + selectedDate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            Toast.makeText(getContext(), "Đã tạo nhắc sinh nhật!\nNgày: " + selectedDate + "\nGiờ: " + time, Toast.LENGTH_LONG).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showReminderDialog() {
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_set_reminder);

        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        TextView tvSelectedDate = dialog.findViewById(R.id.tvSelectedDate);
        Button btnCancel = dialog.findViewById(R.id.btnCancelReminder);
        Button btnCreate = dialog.findViewById(R.id.btnCreateReminder);

        tvSelectedDate.setText("Ngày: " + selectedDate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnCreate.setOnClickListener(v -> {
            try {
                int hour = timePicker.getHour();
                int minute = timePicker.getMinute();
                String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

                long timestamp = parseMillis(selectedDate, time);
                if (timestamp <= System.currentTimeMillis()) {
                    Toast.makeText(getContext(), "Thời gian phải ở tương lai!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String message = "Nhắc nhở lúc " + time + " ngày " + selectedDate;

                // Ghi Firebase
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(userId)
                        .child("reminders");

                String id = ref.push().getKey();
                if (id == null) {
                    Toast.makeText(getContext(), "Không thể tạo ID nhắc hẹn!", Toast.LENGTH_SHORT).show();
                    return;
                }

                HashMap<String, Object> data = new HashMap<>();
                data.put("date", selectedDate);
                data.put("time", time);
                data.put("message", message);
                data.put("timestamp", timestamp);

                ref.child(id).setValue(data)
                        .addOnSuccessListener(aVoid -> {
                            // Đặt báo thức với applicationContext để an toàn vòng đời
                            Context appCtx = requireActivity().getApplicationContext();
                            setAlarm(appCtx, id, timestamp, message);

                            Toast.makeText(getContext(), "Tạo nhắc hẹn thành công!", Toast.LENGTH_LONG).show();

                            if (dialog.isShowing()) dialog.dismiss();
                            loadRemindersForDate();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Lỗi lưu Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (Exception ex) {
                // Bắt mọi exception bất chợt để tránh văng
                Toast.makeText(getContext(), "Lỗi: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


        dialog.show();
    }

    private long parseMillis(String yyyy_MM_dd, String HH_mm) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            Date d = sdf.parse(yyyy_MM_dd + " " + HH_mm);
            return d != null ? d.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }

    private void loadRemindersForDate() {
        if (selectedDate == null) return;

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("reminders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reminderList.clear();
                for (DataSnapshot snap : snapshot.getChildren()) {
                    String date = snap.child("date").getValue(String.class);
                    String time = snap.child("time").getValue(String.class);
                    if (date != null && date.equals(selectedDate)) {
                        reminderList.add("⏰ " + time);
                    }
                }
                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setAlarm(Context context, String reminderId, long timeMillis, String message) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("id", reminderId);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;

        // Android 12+ cần quyền exact alarm
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            if (!am.canScheduleExactAlarms()) {
                try {
                    Intent req = new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    req.setData(android.net.Uri.parse("package:" + context.getPackageName()));
                    req.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(req);
                    // Đặt tạm inexact để không crash; sau khi user cấp quyền sẽ đặt exact được
                    am.set(AlarmManager.RTC_WAKEUP, timeMillis, pi);
                    return;
                } catch (Exception ignored) {
                    // Nếu thiết bị không hỗ trợ intent trên, đặt inexact
                    am.set(AlarmManager.RTC_WAKEUP, timeMillis, pi);
                    return;
                }
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pi);
        }
    }
    private void setVietnameseLocale(Context context) {
        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);

        android.content.res.Configuration config =
                context.getResources().getConfiguration();
        config.setLocale(locale);

        context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
        );
    }

}
