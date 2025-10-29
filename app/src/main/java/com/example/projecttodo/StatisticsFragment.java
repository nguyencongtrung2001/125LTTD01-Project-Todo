package com.example.projecttodo;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StatisticsFragment extends Fragment {

    private TextView tvDaysCount, tvTotalDays;
    private TextView tvTotalTasks, tvCompletedTasks, tvOverdueTasks;
    private PieChartView pieChartView;

    // Sample data
    private int totalTasks = 18;
    private int completedTasks = 15;
    private int overdueTasks = 3;

    private static final String TAG = "StatisticsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        loadStatistics();

        // Force update PieChart sau khi layout measure (thêm listener để đảm bảo kích thước sẵn sàng)
        pieChartView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                pieChartView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                Log.d(TAG, "PieChart measured: width=" + pieChartView.getWidth() + ", height=" + pieChartView.getHeight());
                pieChartView.invalidate(); // Force vẽ lại
            }
        });

        return view;
    }

    private void initViews(View view) {
        tvDaysCount = view.findViewById(R.id.tvDaysCount);
        tvTotalDays = view.findViewById(R.id.tvTotalDays);
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvOverdueTasks = view.findViewById(R.id.tvOverdueTasks);
        pieChartView = view.findViewById(R.id.pieChartView);
        if (pieChartView == null) {
            Log.e(TAG, "PieChartView NOT FOUND! Check XML ID.");
        }
    }

    private void loadStatistics() {
        // TODO: Load real data from database
        // For now, display sample data
        tvDaysCount.setText("7");
        tvTotalDays.setText("30");
        tvTotalTasks.setText(String.valueOf(totalTasks));
        tvCompletedTasks.setText(String.valueOf(completedTasks));
        tvOverdueTasks.setText(String.valueOf(overdueTasks));

        // Update pie chart
        if (pieChartView != null) {
            pieChartView.setData(completedTasks, totalTasks);
            Log.d(TAG, "Set data: completed=" + completedTasks + ", total=" + totalTasks);
        }
    }
}