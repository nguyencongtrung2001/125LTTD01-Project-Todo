package com.example.projecttodo;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView rvCalendarTasks;

    private String selectedDate = null;

    private final String userId = "OeDQ20MOzT2NnC-Tp31";

    private List<String> reminderList = new ArrayList<>();
    private ReminderAdapter reminderAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initViews(view);

        return view;
    }

    private void initViews(View view) {

        calendarView = view.findViewById(R.id.calendarView);
        rvCalendarTasks = view.findViewById(R.id.rvCalendarTasks);
        ImageButton btnAdd = view.findViewById(R.id.btnAdd);

        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
        reminderAdapter = new ReminderAdapter(reminderList);
        rvCalendarTasks.setAdapter(reminderAdapter);

        // KHÔNG load reminder nếu chưa chọn ngày
        // loadRemindersForDate();

        calendarView.setOnDateChangeListener((cv, year, month, dayOfMonth) -> {

            selectedDate = String.format(Locale.getDefault(),
                    "%04d-%02d-%02d",
                    year, month + 1, dayOfMonth);

            Toast.makeText(getContext(),
                    "Ngày đã chọn: " + selectedDate,
                    Toast.LENGTH_SHORT).show();

            loadRemindersForDate();
        });

        btnAdd.setOnClickListener(v -> {

            if (selectedDate == null) {
                Toast.makeText(getContext(),
                        "Vui lòng chọn ngày trong lịch!",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            PopupMenu popup = new PopupMenu(getContext(), btnAdd);
            popup.getMenu().add("Tạo nhắc hẹn");
            popup.getMenu().add("Thêm công việc");
            popup.getMenu().add("Sinh nhật");

            popup.setOnMenuItemClickListener(item -> {
                String choice = item.getTitle().toString();

                if (choice.equals("Tạo nhắc hẹn")) {
                    showReminderDialog();
                }

                if (choice.equals("Thêm công việc")) {
                    Intent intent = new Intent(getContext(), AddTaskActivity.class);
                    intent.putExtra("selectedDate", selectedDate);
                    startActivity(intent);
                }

                if (choice.equals("Sinh nhật")) {
                    showBirthdayDialog();
                }

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
        dialog.setContentView(R.layout.dialog_set_reminder);  // dùng lại layout nhắc hẹn

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

            Toast.makeText(
                    getContext(),
                    "Đã tạo nhắc sinh nhật!\nNgày: " + selectedDate + "\nGiờ: " + time,
                    Toast.LENGTH_LONG
            ).show();

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

            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Không xử lý ngày sai
            String time = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            // ❗ CHỈ HIỆN TOAST — KHÔNG Firebase — KHÔNG AlarmManager
            Toast.makeText(
                    getContext(),
                    "Tạo nhắc hẹn thành công!\nNgày: " + selectedDate + "\nGiờ: " + time,
                    Toast.LENGTH_LONG
            ).show();

            dialog.dismiss();
        });

        dialog.show();
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
                        reminderList.add("⏰" + time);
                    }
                }

                reminderAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void setAlarm(Context context, String reminderId, long timeMillis, String message) {

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("id", reminderId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                reminderId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) return;

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeMillis, pendingIntent);
    }
}
