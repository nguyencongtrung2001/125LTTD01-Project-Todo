package com.example.projecttodo;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm import Log để debug
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.TaskAdapter; // Giữ nguyên import nếu TaskAdapter nằm ở package này
import com.example.projecttodo.Task; // Giữ nguyên import nếu Task nằm ở package này
import com.google.android.material.floatingactionbutton.FloatingActionButton;

// Firebase imports
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment {

    private static final String TAG = "TaskListFragment"; // Tag cho Logcat

    // ... (Khai báo Views và variables không đổi) ...
    private EditText searchEditText;
    private TextView chipAll, chipToday, chipUpcoming, chipOverdue, chipCompleted;
    private LinearLayout emptyStateLayout;
    private RecyclerView taskRecyclerView;
    private FloatingActionButton fabAddTask;
    private String currentFilter = "upcoming";

    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private DatabaseReference databaseReference;

    private final SimpleDateFormat DEADLINE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Khởi tạo Firebase Reference (Tham chiếu đến node "tasks")
        databaseReference = FirebaseDatabase.getInstance().getReference().child("tasks");

        initViews(view);

        // Khởi tạo RecyclerView và Adapter
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>());
        taskRecyclerView.setAdapter(taskAdapter);

        setupChipListeners();

        // Load data lần đầu tiên
        loadAllTasksFromFirebase();

        // Thiết lập chip mặc định là 'upcoming'
        selectChip(currentFilter);

        return view;
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        chipAll = view.findViewById(R.id.chipAll);
        chipToday = view.findViewById(R.id.chipToday);
        chipUpcoming = view.findViewById(R.id.chipUpcoming);
        chipOverdue = view.findViewById(R.id.chipOverdue);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        fabAddTask = view.findViewById(R.id.fabAddTask);
    }

    private void setupChipListeners() {
        // Sử dụng key ngắn gọn để lọc: "all", "today", "upcoming", ...

        chipAll.setOnClickListener(v -> {
            selectChip("all");
            Toast.makeText(getContext(), "Tất cả", Toast.LENGTH_SHORT).show();
        });

        chipToday.setOnClickListener(v -> {
            selectChip("today");
            Toast.makeText(getContext(), "Hôm nay", Toast.LENGTH_SHORT).show();
        });

        chipUpcoming.setOnClickListener(v -> {
            selectChip("upcoming");
            Toast.makeText(getContext(), "Sắp tới", Toast.LENGTH_SHORT).show();
        });

        chipOverdue.setOnClickListener(v -> {
            selectChip("overdue");
            Toast.makeText(getContext(), "Quá hạn", Toast.LENGTH_SHORT).show();
        });

        chipCompleted.setOnClickListener(v -> {
            selectChip("completed");
            Toast.makeText(getContext(), "Hoàn thành", Toast.LENGTH_SHORT).show();
        });

        fabAddTask.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), AddTaskActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không thể mở màn hình thêm task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectChip(String filter) {
        currentFilter = filter;

        // Reset tất cả chips về trạng thái mặc định
        chipAll.setBackgroundResource(R.drawable.chip_background);
        chipToday.setBackgroundResource(R.drawable.chip_background);
        chipUpcoming.setBackgroundResource(R.drawable.chip_background);
        chipOverdue.setBackgroundResource(R.drawable.chip_background);
        chipCompleted.setBackgroundResource(R.drawable.chip_background);

        // Set chip được chọn
        switch (filter) {
            case "all":
                chipAll.setBackgroundResource(R.drawable.chip_selected_background);
                break;
            case "today":
                chipToday.setBackgroundResource(R.drawable.chip_selected_background);
                break;
            case "upcoming":
                chipUpcoming.setBackgroundResource(R.drawable.chip_selected_background);
                break;
            case "overdue":
                chipOverdue.setBackgroundResource(R.drawable.chip_selected_background);
                break;
            case "completed":
                chipCompleted.setBackgroundResource(R.drawable.chip_selected_background);
                break;
        }

        updateTaskList();
    }

    private void loadAllTasksFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Debug: Kiểm tra số lượng task tải về
                Log.d(TAG, "Tasks Loaded: " + snapshot.getChildrenCount());

                allTasks.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        if (task != null) {
                            allTasks.add(task);
                        } else {
                            // Debug: Nếu task null, có thể do lỗi ánh xạ Model
                            Log.e(TAG, "Task mapping failed for key: " + taskSnapshot.getKey());
                        }
                    }
                }
                updateTaskList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải dữ liệu Firebase: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void updateTaskList() {
        List<Task> filteredTasks = new ArrayList<>();
        Date now = new Date();
        String todayDateString = DATE_ONLY_FORMAT.format(now);

        for (Task task : allTasks) {
            boolean matchesFilter = false;
            Date taskDeadlineDate = null;

            try {
                // Phân tích chuỗi deadline
                taskDeadlineDate = DEADLINE_FORMAT.parse(task.getdeadline());
            } catch (ParseException e) {
                Log.e(TAG, "Deadline parsing error for task: " + task.getTitle(), e);
                continue; // Bỏ qua task nếu deadline không hợp lệ
            }

            String taskDeadlineDateString = DATE_ONLY_FORMAT.format(taskDeadlineDate);

            // LOGIC LỌC ĐÃ SỬA: Dùng key ngắn gọn
            switch (currentFilter) {
                case "all":
                    matchesFilter = true;
                    break;

                case "today":
                    matchesFilter = taskDeadlineDateString.equals(todayDateString);
                    break;

                case "upcoming":
                    boolean isFuture = taskDeadlineDate.after(now);
                    boolean isToday = taskDeadlineDateString.equals(todayDateString);

                    // Task chưa hoàn thành, ở tương lai, VÀ không phải là ngày hôm nay
                    matchesFilter = !task.isCompleted() && isFuture && !isToday;
                    break;

                case "overdue":
                    // Task chưa hoàn thành VÀ đã quá hạn
                    matchesFilter = !task.isCompleted() && taskDeadlineDate.before(now);
                    break;

                case "completed":
                    matchesFilter = task.isCompleted();
                    break;
            }

            if (matchesFilter) {
                filteredTasks.add(task);
            }
        }

        // Cập nhật dữ liệu cho Adapter
        taskAdapter.updateTasks(filteredTasks);

        // Hiển thị/Ẩn Empty State
        if (filteredTasks.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}