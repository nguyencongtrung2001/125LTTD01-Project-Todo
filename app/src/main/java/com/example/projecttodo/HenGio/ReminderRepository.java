package com.example.projecttodo.HenGio;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Lưu reminder vào Firebase: users/<uid>/reminders/<key> */
public class ReminderRepository {

    public static void saveReminder(Context ctx, long timestampMillis, String message) {
        // Lấy user_id từ session (đúng như bạn đã lưu trong LoginActivity)
        SharedPreferences sp = ctx.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String uid = sp.getString("user_id", "null");
        if (uid == null || "null".equals(uid)) {
            Toast.makeText(ctx, "Chưa có user_id, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        // Tạo các trường date/time theo format bạn đang dùng
        SimpleDateFormat dfDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat dfTime = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String dateStr = dfDate.format(timestampMillis);
        String timeStr = dfTime.format(timestampMillis);

        Map<String, Object> data = new HashMap<>();
        data.put("date", dateStr);
        data.put("time", timeStr);
        data.put("message", (message == null || message.isEmpty()) ? ("Nhắc nhở lúc " + timeStr) : message);
        data.put("timestamp", timestampMillis);

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users").child(uid).child("reminders");

        String key = ref.push().getKey();
        Log.d("DB_WRITE", "path=" + ref + " key=" + key);

        ref.child(key).setValue(data)
                .addOnSuccessListener(unused -> Toast.makeText(ctx, "Đã tạo nhắc hẹn!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(ctx, "Lỗi tạo nhắc hẹn: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
