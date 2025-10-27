package com.example.projecttodo;

import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TaskListFragment extends Fragment {

    private EditText searchEditText;
    private TextView chipAll, chipToday, chipUpcoming, chipOverdue, chipCompleted;
    private LinearLayout emptyStateLayout;
    private RecyclerView taskRecyclerView;
    private FloatingActionButton fabAddTask;

    private String currentFilter = "upcoming";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        initViews(view);
        setupChipListeners();
        setupFabListener();

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

        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getContext(), AddTaskActivity.class);
                startActivity(intent);
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

        // Cập nhật danh sách task
        updateTaskList();
    }

    private void updateTaskList() {
        // Hiện tại hiển thị empty state
        // Sau này có thể thêm logic hiển thị danh sách task
        emptyStateLayout.setVisibility(View.VISIBLE);
        taskRecyclerView.setVisibility(View.GONE);
    }

    private void setupFabListener() {
        fabAddTask.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Thêm nhiệm vụ mới", Toast.LENGTH_SHORT).show();
            // Có thể mở dialog hoặc activity mới để thêm task
        });
    }
}