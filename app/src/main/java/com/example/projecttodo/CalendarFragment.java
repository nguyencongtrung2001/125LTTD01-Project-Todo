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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.HenGio.ReminderReceiver;
import com.example.projecttodo.Model.Reminder;
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
    private TextView tvDayInfo;
    private String userId = "";
    private final List<Reminder> reminderList = new ArrayList<>();
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
        tvDayInfo = view.findViewById(R.id.tvDayInfo);
        calendarView = view.findViewById(R.id.calendarView);
        calendarView.setDate(System.currentTimeMillis(), false, true);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(new Date());
        loadRemindersForDate();

        rvCalendarTasks = view.findViewById(R.id.rvCalendarTasks);
        ImageButton btnAdd = view.findViewById(R.id.btnAdd);

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderAdapter = new ReminderAdapter(reminderList, reminder -> {
            if ("birthday".equals(reminder.type)) {
                showEditBirthdayDialog(reminder);
            } else {
                showEditReminderDialog(reminder);
            }
        });

        rvCalendarTasks.setAdapter(reminderAdapter);

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderAdapter = new ReminderAdapter(reminderList, reminder -> {
            if ("birthday".equals(reminder.type)) {
                showEditBirthdayDialog(reminder);
            } else {
                showEditReminderDialog(reminder);
            }
        });

        rvCalendarTasks.setAdapter(reminderAdapter);
        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {

            // ===== CHECK NGÀY QUÁ KHỨ =====
            java.util.Calendar selectedCal = java.util.Calendar.getInstance();
            selectedCal.set(year, month, dayOfMonth, 0, 0, 0);
            selectedCal.set(java.util.Calendar.MILLISECOND, 0);

            java.util.Calendar today = java.util.Calendar.getInstance();
            today.set(java.util.Calendar.HOUR_OF_DAY, 0);
            today.set(java.util.Calendar.MINUTE, 0);
            today.set(java.util.Calendar.SECOND, 0);
            today.set(java.util.Calendar.MILLISECOND, 0);

            if (selectedCal.before(today)) {
                Toast.makeText(
                        getContext(),
                        "Vui lòng chọn ngày trong tương lai",
                        Toast.LENGTH_SHORT
                ).show();

                // quay lại ngày hôm nay
                calendarView.setDate(System.currentTimeMillis(), false, true);
                return;
            }
            // ✅ Ngày hợp lệ
            selectedDate = String.format(
                    Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year, month + 1, dayOfMonth
            );
            Toast.makeText(
                    getContext(),
                    "Ngày đã chọn: " + toDisplayDate(selectedDate),
                    Toast.LENGTH_SHORT
            ).show();
            loadRemindersForDate();
        });
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

    private void showEditBirthdayDialog(Reminder reminder) {
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_birthday);
        Button btnDelete = dialog.findViewById(R.id.btnDeleteBirthday);
        EditText edtName = dialog.findViewById(R.id.edtBirthdayName);
        EditText edtDetail = dialog.findViewById(R.id.edtBirthdayDetail);
        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        Button btnCancel = dialog.findViewById(R.id.btnCancelBirthday);
        Button btnSave = dialog.findViewById(R.id.btnCreateBirthday);

        timePicker.setIs24HourView(true);

        // ===== SET DỮ LIỆU CŨ =====
        if (reminder.name != null)
            edtName.setText(reminder.name);

        if (reminder.detail != null)
            edtDetail.setText(reminder.detail);

        if (reminder.time != null && reminder.time.contains(":")) {
            String[] parts = reminder.time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            timePicker.setHour(hour);
            timePicker.setMinute(minute);
        }

        // ===== HUỶ =====
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Xoá sinh nhật")
                    .setMessage("Bạn có chắc muốn xoá sinh nhật này không?")
                    .setPositiveButton("Xoá", (d, w) -> {

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("reminders")
                                .child(reminder.id)
                                .removeValue()
                                .addOnSuccessListener(a -> {
                                    Toast.makeText(getContext(),
                                            "Đã xoá sinh nhật", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    loadRemindersForDate();
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        // ===== LƯU =====
        btnSave.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String detail = edtDetail.getText().toString().trim();

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String newTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            long newTimestamp = parseMillis(reminder.date, newTime);

            if (newTimestamp <= System.currentTimeMillis()) {
                Toast.makeText(getContext(), "Thời gian phải ở tương lai!", Toast.LENGTH_SHORT).show();
                return;
            }
            //xóa
            btnDelete.setOnClickListener(view -> {
                new AlertDialog.Builder(getContext())
                        .setTitle("Xoá sinh nhật")
                        .setMessage("Bạn có chắc muốn xoá sinh nhật này không?")
                        .setPositiveButton("Xoá", (d, w) -> {

                            FirebaseDatabase.getInstance()
                                    .getReference("users")
                                    .child(userId)
                                    .child("reminders")
                                    .child(reminder.id)
                                    .removeValue()
                                    .addOnSuccessListener(a -> {
                                        Toast.makeText(getContext(),
                                                "Đã xoá sinh nhật", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                        loadRemindersForDate();
                                    });
                        })
                        .setNegativeButton("Huỷ", null)
                        .show();
            });
            // ===== UPDATE FIREBASE =====
            HashMap<String, Object> update = new HashMap<>();
            update.put("name", name);
            update.put("detail", detail);
            update.put("time", newTime);
            update.put("timestamp", newTimestamp);
            update.put("message", "Sinh nhật " + name + " lúc " + newTime);

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("reminders")
                    .child(reminder.id)
                    .updateChildren(update)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Đã cập nhật sinh nhật", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRemindersForDate();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        dialog.show();
    }


    private void showBirthdayDialog() {
        if (selectedDate == null) {
            Toast.makeText(getContext(), "Vui lòng chọn ngày!", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.dialog_birthday);

        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);

        TextView tvSelectedDate = dialog.findViewById(R.id.tvSelectedDate);
        Button btnCancel = dialog.findViewById(R.id.btnCancelBirthday);
        Button btnCreate = dialog.findViewById(R.id.btnCreateBirthday);

        android.widget.EditText edtName = dialog.findViewById(R.id.edtBirthdayName);
        android.widget.EditText edtDetail = dialog.findViewById(R.id.edtBirthdayDetail);

        tvSelectedDate.setText("🎂 Sinh nhật: " + toDisplayDate(selectedDate)); // nếu bạn đã có toDisplayDate()

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            String name = edtName.getText().toString().trim();
            String detail = edtDetail.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên người sinh nhật!", Toast.LENGTH_SHORT).show();
                return;
            }

            long timestamp = parseMillis(selectedDate, time);

            DatabaseReference ref = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("reminders");

            String id = ref.push().getKey();
            if (id == null) {
                Toast.makeText(getContext(), "Không thể tạo ID!", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> data = new HashMap<>();
            data.put("date", selectedDate);
            data.put("time", time);
            data.put("type", "birthday");
            data.put("name", name);
            data.put("detail", detail);
            data.put("message", "🎂 Sinh nhật " + name); // để notification/hiển thị nhanh
            data.put("timestamp", timestamp);

            ref.child(id).setValue(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "🎂 Đã tạo nhắc sinh nhật!", Toast.LENGTH_SHORT).show();
                        loadRemindersForDate(); // cập nhật list
                        dialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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


        EditText edtTitle = dialog.findViewById(R.id.edtReminderTitle);

        tvSelectedDate.setText("Ngày: " + toDisplayDate(selectedDate));

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


                String title = edtTitle.getText().toString().trim();
                if (title.isEmpty()) {
                    title = "Nhắc hẹn";
                }

                String message = title + " lúc " + time;

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
                data.put("title", title);
                data.put("message", message);
                data.put("timestamp", timestamp);

                ref.child(id).setValue(data)
                        .addOnSuccessListener(aVoid -> {

                            Context appCtx = requireActivity().getApplicationContext();
                            setAlarm(appCtx, id, timestamp, message);

                            Toast.makeText(getContext(), "Tạo nhắc hẹn thành công!", Toast.LENGTH_LONG).show();

                            if (dialog.isShowing()) dialog.dismiss();
                            loadRemindersForDate();
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Lỗi lưu Firebase: " + e.getMessage(), Toast.LENGTH_SHORT).show());

            } catch (Exception ex) {
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
                    Reminder r = snap.getValue(Reminder.class);
                    if (r == null) continue;

                    r.id = snap.getKey();

                    if (selectedDate.equals(r.date)) {
                        reminderList.add(r);
                    }
                }

                int count = reminderList.size();
                if (count > 0) {
                    tvDayInfo.setText("🔔 Ngày này có " + count + " nhắc hẹn");
                } else {
                    tvDayInfo.setText("📅 Ngày này chưa có nhắc hẹn");
                }

                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void showEditReminderDialog(Reminder reminder) {
        Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_set_reminder);

        TimePicker timePicker = dialog.findViewById(R.id.timePicker);
        EditText edtTitle = dialog.findViewById(R.id.edtReminderTitle);
        Button btnSave = dialog.findViewById(R.id.btnCreateReminder);
        Button btnCancel = dialog.findViewById(R.id.btnCancelReminder);
        Button btnDelete = dialog.findViewById(R.id.btnDeleteReminder);

        timePicker.setIs24HourView(true);

        // ===== SET DỮ LIỆU CŨ =====
        if (reminder.title != null) {
            edtTitle.setText(reminder.title);
        }

        if (reminder.time != null && reminder.time.contains(":")) {
            String[] parts = reminder.time.split(":");
            timePicker.setHour(Integer.parseInt(parts[0]));
            timePicker.setMinute(Integer.parseInt(parts[1]));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // ===== LƯU =====
        btnSave.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String newTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            long newTimestamp = parseMillis(reminder.date, newTime);

            HashMap<String, Object> update = new HashMap<>();
            update.put("time", newTime);
            update.put("timestamp", newTimestamp);
            update.put("title", edtTitle.getText().toString().trim());

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("reminders")
                    .child(reminder.id)
                    .updateChildren(update)
                    .addOnSuccessListener(a -> {
                        Toast.makeText(getContext(), "Đã cập nhật nhắc hẹn", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadRemindersForDate();
                    });
        });

        // ===== XOÁ =====
        btnDelete.setOnClickListener(view -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Xoá nhắc hẹn")
                    .setMessage("Bạn có chắc muốn xoá nhắc hẹn này không?")
                    .setPositiveButton("Xoá", (dialogInterface, which) -> {

                        // ✅ reminder NẰM TRONG SCOPE
                        cancelAlarm(requireContext(), reminder.id);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("reminders")
                                .child(reminder.id)
                                .removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(),
                                            "Đã xoá nhắc hẹn", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    loadRemindersForDate();
                                });
                    })
                    .setNegativeButton("Huỷ", null)
                    .show();
        });

        dialog.show();
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
    private static final SimpleDateFormat STORE_FMT =
            new SimpleDateFormat("yyyy-MM-dd", new Locale("vi", "VN"));
    private static final SimpleDateFormat DISPLAY_FMT =
            new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

    private String toDisplayDate(String storeDate) {
        try {
            SimpleDateFormat in =
                    new SimpleDateFormat("yyyy-MM-dd", new Locale("vi", "VN"));
            SimpleDateFormat out =
                    new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
            Date d = in.parse(storeDate);
            return d != null ? out.format(d) : storeDate;
        } catch (Exception e) {
            return storeDate;
        }
    }

    private void cancelAlarm(Context context, String reminderId) {
        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            am.cancel(pi);
        }
    }

}
