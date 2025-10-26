package com.example.projecttodo;

package com.example.myapp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class CustomReminderDialog extends Dialog {

    private EditText etDays, etHours, etMinutes;
    private Button btn5Minutes, btn10Minutes, btn15Minutes;
    private Button btn30Minutes, btn1Hour, btn2Hours;
    private Button btn1Day, btn3Days, btn1Week;
    private Button btnCancelReminder, btnSaveReminder;

    private OnReminderSetListener listener;
    private int days = 0, hours = 0, minutes = 0;

    public interface OnReminderSetListener {
        void onReminderSet(int days, int hours, int minutes);
    }

    public CustomReminderDialog(@NonNull Context context, OnReminderSetListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_custom_reminder);

        // Set dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        etDays = findViewById(R.id.etDays);
        etHours = findViewById(R.id.etHours);
        etMinutes = findViewById(R.id.etMinutes);

        btn5Minutes = findViewById(R.id.btn5Minutes);
        btn10Minutes = findViewById(R.id.btn10Minutes);
        btn15Minutes = findViewById(R.id.btn15Minutes);
        btn30Minutes = findViewById(R.id.btn30Minutes);
        btn1Hour = findViewById(R.id.btn1Hour);
        btn2Hours = findViewById(R.id.btn2Hours);
        btn1Day = findViewById(R.id.btn1Day);
        btn3Days = findViewById(R.id.btn3Days);
        btn1Week = findViewById(R.id.btn1Week);

        btnCancelReminder = findViewById(R.id.btnCancelReminder);
        btnSaveReminder = findViewById(R.id.btnSaveReminder);
    }

    private void setupListeners() {
        // Common time quick buttons
        btn5Minutes.setOnClickListener(v -> setTime(0, 0, 5));
        btn10Minutes.setOnClickListener(v -> setTime(0, 0, 10));
        btn15Minutes.setOnClickListener(v -> setTime(0, 0, 15));
        btn30Minutes.setOnClickListener(v -> setTime(0, 0, 30));
        btn1Hour.setOnClickListener(v -> setTime(0, 1, 0));
        btn2Hours.setOnClickListener(v -> setTime(0, 2, 0));
        btn1Day.setOnClickListener(v -> setTime(1, 0, 0));
        btn3Days.setOnClickListener(v -> setTime(3, 0, 0));
        btn1Week.setOnClickListener(v -> setTime(7, 0, 0));

        btnCancelReminder.setOnClickListener(v -> dismiss());

        btnSaveReminder.setOnClickListener(v -> {
            try {
                days = Integer.parseInt(etDays.getText().toString());
                hours = Integer.parseInt(etHours.getText().toString());
                minutes = Integer.parseInt(etMinutes.getText().toString());

                // Validation
                if (days < 0 || hours < 0 || minutes < 0) {
                    Toast.makeText(getContext(), "Giá trị phải lớn hơn hoặc bằng 0", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hours > 23) {
                    Toast.makeText(getContext(), "Giờ không được vượt quá 23", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (minutes > 59) {
                    Toast.makeText(getContext(), "Phút không được vượt quá 59", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (days == 0 && hours == 0 && minutes == 0) {
                    Toast.makeText(getContext(), "Vui lòng chọn thời gian nhắc nhở", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (listener != null) {
                    listener.onReminderSet(days, hours, minutes);
                }

                String reminderText = formatReminderTime(days, hours, minutes);
                Toast.makeText(getContext(), "Đã đặt nhắc nhở: " + reminderText, Toast.LENGTH_SHORT).show();
                dismiss();

            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Vui lòng nhập số hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTime(int d, int h, int m) {
        etDays.setText(String.valueOf(d));
        etHours.setText(String.valueOf(h));
        etMinutes.setText(String.valueOf(m));
    }

    private String formatReminderTime(int days, int hours, int minutes) {
        StringBuilder sb = new StringBuilder();

        if (days > 0) {
            sb.append(days).append(" ngày ");
        }
        if (hours > 0) {
            sb.append(hours).append(" giờ ");
        }
        if (minutes > 0) {
            sb.append(minutes).append(" phút");
        }

        return sb.toString().trim();
    }
}
