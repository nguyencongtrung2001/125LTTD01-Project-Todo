package com.example.projecttodo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.adapter.TaskAdapter;
import com.example.projecttodo.Model.Task;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class TaskListFragment extends Fragment {

    private RecyclerView taskRecyclerView;
    private TaskAdapter taskAdapter;
    private LinearLayout emptyStateLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        FloatingActionButton fabAddTask = view.findViewById(R.id.fabAddTask);

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddTaskActivity.class);
            startActivity(intent);
        });

        setupRecyclerView();

        return view;
    }

    private void setupRecyclerView() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId == null) {
            updateEmptyState(true);
            return;
        }

        Query query = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tasks");

        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>()
                .setQuery(query, Task.class)
                .build();

        taskAdapter = new TaskAdapter(options) {
            @Override
            public void onDataChanged() {
                super.onDataChanged();
                updateEmptyState(getItemCount() == 0);
            }
        };
        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        taskRecyclerView.setAdapter(taskAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (taskAdapter != null) {
            taskAdapter.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (taskAdapter != null) {
            taskAdapter.stopListening();
        }
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }
}
