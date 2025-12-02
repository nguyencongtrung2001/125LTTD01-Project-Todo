package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    private static final String TAG = "TaskListFragment";

    private EditText searchEditText;
    private TextView chipAll, chipToday, chipUpcoming, chipOverdue, chipCompleted;
    private LinearLayout emptyStateLayout;
    private RecyclerView taskRecyclerView;
    private FloatingActionButton fabAddTask;

    private String userId;
    private String currentFilter = "all"; // default: show all
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private DatabaseReference databaseReference;

    private final SimpleDateFormat DEADLINE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        // Lấy userId từ SharedPreferences
        loadUserSession();

        // Trỏ vào Firebase tasks của user
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tasks");

        initViews(view);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>());
        taskRecyclerView.setAdapter(taskAdapter);

        setupChipListeners();

        loadAllTasksFromFirebase();
        selectChip(currentFilter); // default

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

    private void loadUserSession() {
        SharedPreferences prefs = requireContext()
                .getSharedPreferences("user_session", requireContext().MODE_PRIVATE);
        userId = prefs.getString("user_id", "");
    }

    private void setupChipListeners() {
        chipAll.setOnClickListener(v -> selectChip("all"));
        chipToday.setOnClickListener(v -> selectChip("today"));
        chipUpcoming.setOnClickListener(v -> selectChip("upcoming"));
        chipOverdue.setOnClickListener(v -> selectChip("overdue"));
        chipCompleted.setOnClickListener(v -> selectChip("completed"));

        fabAddTask.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddTaskActivity.class));
            } else {
                Toast.makeText(getContext(), "Không thể mở màn hình thêm task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectChip(String filter) {
        currentFilter = filter;

        // Reset tất cả chip về mặc định
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
                allTasks.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        if (task != null) {
                            allTasks.add(task);
                        } else {
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
                taskDeadlineDate = DEADLINE_FORMAT.parse(task.getDeadline());
            } catch (ParseException e) {
                Log.e(TAG, "Deadline parsing error for task: " + task.getTitle(), e);
                continue;
            }

            String taskDateStr = DATE_ONLY_FORMAT.format(taskDeadlineDate);

            switch (currentFilter) {
                case "all":
                    matchesFilter = true;
                    break;
                case "today":
                    matchesFilter = taskDateStr.equals(todayDateString);
                    break;
                case "upcoming":
                    matchesFilter = !task.isCompleted() &&
                            taskDeadlineDate.after(now) &&
                            !taskDateStr.equals(todayDateString);
                    break;
                case "overdue":
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

        taskAdapter.updateTasks(filteredTasks);

        if (filteredTasks.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
