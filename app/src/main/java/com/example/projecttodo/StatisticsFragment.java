package com.example.projecttodo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StatisticsFragment extends Fragment {

    private TextView tvDaysCount, tvTotalDays;
    private TextView tvTotalTasks, tvCompletedTasks, tvOverdueTasks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        loadStatistics();

        return view;
    }

    private void initViews(View view) {
        tvDaysCount = view.findViewById(R.id.tvDaysCount);
        tvTotalDays = view.findViewById(R.id.tvTotalDays);
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvOverdueTasks = view.findViewById(R.id.tvOverdueTasks);
    }

    private void loadStatistics() {
        // TODO: Load real data from database
        // For now, display sample data
        tvDaysCount.setText("7");
        tvTotalDays.setText("30");
        tvTotalTasks.setText("18");
        tvCompletedTasks.setText("15");
        tvOverdueTasks.setText("3");
    }
}