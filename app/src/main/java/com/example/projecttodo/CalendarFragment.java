package com.example.projecttodo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private TextView tvMonth, tvYear;
    private ImageButton btnPreviousMonth, btnNextMonth;
    private CalendarView calendarView;
    private RecyclerView rvCalendarTasks;

    private Calendar currentCalendar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        initViews(view);
        setupCalendar();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvMonth = view.findViewById(R.id.tvMonth);
        tvYear = view.findViewById(R.id.tvYear);
        btnPreviousMonth = view.findViewById(R.id.btnPreviousMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        calendarView = view.findViewById(R.id.calendarView);
        rvCalendarTasks = view.findViewById(R.id.rvCalendarTasks);

        // Setup RecyclerView
        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupCalendar() {
        currentCalendar = Calendar.getInstance();
        updateMonthYearDisplay();
    }

    private void setupListeners() {
        btnPreviousMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateMonthYearDisplay();
            updateCalendarView();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateMonthYearDisplay();
            updateCalendarView();
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Load tasks for selected date
            loadTasksForDate(year, month, dayOfMonth);
        });
    }

    private void updateMonthYearDisplay() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("'Tháng' M", new Locale("vi", "VN"));
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy", Locale.getDefault());

        tvMonth.setText(monthFormat.format(currentCalendar.getTime()));
        tvYear.setText(yearFormat.format(currentCalendar.getTime()));
    }

    private void updateCalendarView() {
        calendarView.setDate(currentCalendar.getTimeInMillis(), false, true);
    }

    private void loadTasksForDate(int year, int month, int day) {
        // TODO: Load tasks from database for selected date
        // For now, just show empty list
    }
}