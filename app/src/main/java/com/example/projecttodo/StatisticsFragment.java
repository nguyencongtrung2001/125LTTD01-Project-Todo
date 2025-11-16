package com.example.projecttodo;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatisticsFragment extends Fragment {

    private TextView tvTotalTasks, tvCompletedTasks, tvOverdueTasks;
    private TextView btnAll, btn7Days, btn30Days;
    private PieChartView pieChartView;

    private String userId;
    private DatabaseReference userRef;

    private static final String TAG = "StatisticsFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        initViews(view);
        loadUserSession();

        highlightSelected(btnAll);
        loadStatistics("_all");   // đúng key Firebase

        setupClicks();

        return view;
    }

    private void initViews(View view) {
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvOverdueTasks = view.findViewById(R.id.tvOverdueTasks);

        btnAll = view.findViewById(R.id.btnAll);
        btn7Days = view.findViewById(R.id.btn7Days);
        btn30Days = view.findViewById(R.id.btn30Days);

        pieChartView = view.findViewById(R.id.pieChartView);
    }

    private void loadUserSession() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("user_session", requireContext().MODE_PRIVATE);

        userId = prefs.getString("user_id", "");

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("statistics");
    }

    private void setupClicks() {

        btnAll.setOnClickListener(v -> {
            highlightSelected(btnAll);
            loadStatistics("all");
        });

        btn7Days.setOnClickListener(v -> {
            highlightSelected(btn7Days);
            loadStatistics("last7Days");
        });

        btn30Days.setOnClickListener(v -> {
            highlightSelected(btn30Days);
            loadStatistics("last30Days");
        });
    }

    private void highlightSelected(TextView selected) {

        btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        btn7Days.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));
        btn30Days.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_gray));

        selected.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_500));
    }

    private void loadStatistics(String key) {

        userRef.child(key).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                Log.e(TAG, "Load error");
                return;
            }

            DataSnapshot s = task.getResult();
            if (s == null) return;

            int total = s.child("total").getValue(Integer.class) != null ?
                    s.child("total").getValue(Integer.class) : 0;

            int completed = s.child("completed").getValue(Integer.class) != null ?
                    s.child("completed").getValue(Integer.class) : 0;

            int overdue = s.child("overdue").getValue(Integer.class) != null ?
                    s.child("overdue").getValue(Integer.class) : 0;

            int incomplete = Math.max(0, total - completed); // auto tính

            // cập nhật UI
            tvTotalTasks.setText(String.valueOf(total));
            tvCompletedTasks.setText(String.valueOf(completed));
            tvOverdueTasks.setText(String.valueOf(overdue));

            // cập nhật biểu đồ
            pieChartView.setData(completed, total);

        });
    }
    @Override
    public void onResume() {
        super.onResume();

        if (userId != null && !userId.isEmpty()) {
            highlightSelected(btnAll);
            loadStatistics("all");
        }
    }

}
