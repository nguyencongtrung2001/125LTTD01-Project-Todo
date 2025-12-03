package com.example.projecttodo.ThongKe;

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

import com.example.projecttodo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatisticsFragment extends Fragment {

    private TextView tvTotalTasks, tvCompletedTasks, tvIncompleteTasks, tvOverdueTasks;
    private TextView btnAll, btn7Days, btn30Days;
    private TextView tvBarChartTitle;

    private PieChartView pieChartView;
    private BarChartView barChartView;

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
        loadStatistics("all");  // mặc định tab "Tất cả"

        setupClicks();

        return view;
    }

    private void initViews(View view) {
        tvTotalTasks = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = view.findViewById(R.id.tvCompletedTasks);
        tvIncompleteTasks = view.findViewById(R.id.tvIncompleteTasks);
        tvOverdueTasks = view.findViewById(R.id.tvOverdueTasks);

        btnAll = view.findViewById(R.id.btnAll);
        btn7Days = view.findViewById(R.id.btn7Days);
        btn30Days = view.findViewById(R.id.btn30Days);

        pieChartView = view.findViewById(R.id.pieChartView);
        barChartView = view.findViewById(R.id.barChartView);

        tvBarChartTitle = view.findViewById(R.id.tvBarChartTitle);
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

        // lấy data đúng key: "all", "last7Days", "last30Days"
        userRef.child(key).get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                Log.e(TAG, "Load error", task.getException());
                return;
            }

            DataSnapshot s = task.getResult();
            if (s == null) return;

            // Đọc dữ liệu, nếu null thì = 0, đảm bảo không bị NPE
            int total = getIntValue(s, "total");
            int completed = getIntValue(s, "completed");
            int incomplete = getIntValue(s, "incomplete");
            int overdue = getIntValue(s, "overdue");

            // Clamp cho chắc (nếu DB lỡ để -1, -2,...)
            completed = Math.max(0, completed);
            incomplete = Math.max(0, incomplete);
            overdue = Math.max(0, overdue);
            total = Math.max(0, total);

            // Tính lại tổng dựa trên 3 trường trạng thái
            int sumStates = completed + incomplete + overdue;

            // UI tổng số việc:
            // - nếu sumStates > 0: ưu tiên hiển thị sumStates (vì logic chắc chắn)
            // - nếu sumStates = 0: hiển thị total (trường hợp chưa có dữ liệu trạng thái)
            int displayTotal = (sumStates > 0) ? sumStates : total;

            // Cập nhật UI text
            tvTotalTasks.setText(String.valueOf(displayTotal));
            tvCompletedTasks.setText(String.valueOf(completed));
            tvIncompleteTasks.setText(String.valueOf(incomplete));
            tvOverdueTasks.setText(String.valueOf(overdue));

            // === Biểu đồ tròn ===
            // PieChartView tự tính % dựa trên completed + incomplete + overdue
            pieChartView.setData(completed, incomplete, overdue);

            // === Biểu đồ cột ===
            // Cột "Tất cả" nên dùng displayTotal cho khớp với card tổng
            if (barChartView != null) {
                barChartView.setData(displayTotal, completed, incomplete, overdue);
            }

            // === Tên biểu đồ dưới BarChart ===
            String title;
            switch (key) {
                case "last7Days":
                    title = "Số lượng công việc 7 ngày gần nhất";
                    break;
                case "last30Days":
                    title = "Số lượng công việc 30 ngày gần nhất";
                    break;
                default:
                    title = "Tất cả công việc của bạn";
                    break;
            }
            tvBarChartTitle.setText(title);

        });
    }

    private int getIntValue(DataSnapshot parent, String childKey) {
        Integer v = parent.child(childKey).getValue(Integer.class);
        return v != null ? v : 0;
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
