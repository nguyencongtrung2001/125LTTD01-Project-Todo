package com.example.projecttodo.HenGio;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.projecttodo.R;

import java.util.Calendar;
import java.util.Locale;

public class ReminderDialog extends DialogFragment {

    private static final String ARG_DATE_MILLIS = "ARG_DATE_MILLIS";

    public static ReminderDialog newInstance(@Nullable Long dateMillis) {
        ReminderDialog d = new ReminderDialog();
        Bundle b = new Bundle();
        if (dateMillis != null) b.putLong(ARG_DATE_MILLIS, dateMillis);
        d.setArguments(b);
        return d;
    }

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_set_reminder);
        dialog.setCanceledOnTouchOutside(false);

        TextView tvSelectedDate = dialog.findViewById(R.id.tvSelectedDate);
        TimePicker timePicker   = dialog.findViewById(R.id.timePicker);
        Button btnCancel        = dialog.findViewById(R.id.btnCancelReminder);
        Button btnCreate        = dialog.findViewById(R.id.btnCreateReminder);

        timePicker.setIs24HourView(DateFormat.is24HourFormat(requireContext()));

        Calendar base = Calendar.getInstance();
        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_DATE_MILLIS)) {
            base.setTimeInMillis(args.getLong(ARG_DATE_MILLIS));
        }

        String dateLabel = String.format(Locale.getDefault(),
                "Ngày: %1$tY-%1$tm-%1$td", base);
        tvSelectedDate.setText(dateLabel);

        btnCancel.setOnClickListener(v -> dismiss());
        btnCreate.setOnClickListener(v -> {
            int hour, minute;
            if (Build.VERSION.SDK_INT >= 23) {
                hour = timePicker.getHour();
                minute = timePicker.getMinute();
            } else {
                hour = timePicker.getCurrentHour();
                minute = timePicker.getCurrentMinute();
            }

            Calendar selected = (Calendar) base.clone();
            selected.set(Calendar.HOUR_OF_DAY, hour);
            selected.set(Calendar.MINUTE, minute);
            selected.set(Calendar.SECOND, 0);
            selected.set(Calendar.MILLISECOND, 0);

            long when = selected.getTimeInMillis();
            String message = "Nhắc nhở lúc " + String.format(Locale.getDefault(), "%02d:%02d", hour, minute);

            ReminderRepository.saveReminder(requireContext(), when, message);
            dismiss();
        });

        return dialog;
    }
}
