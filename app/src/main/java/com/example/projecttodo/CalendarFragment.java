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

    private CalendarView calendarView;
    private RecyclerView rvCalendarTasks;

    private Calendar currentCalendar;

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

        // Setup RecyclerView
        rvCalendarTasks.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void updateCalendarView() {
        calendarView.setDate(currentCalendar.getTimeInMillis(), false, true);
    }

    private void loadTasksForDate(int year, int month, int day) {
        // TODO: Load tasks from database for selected date
        // For now, just show empty list
    }
}