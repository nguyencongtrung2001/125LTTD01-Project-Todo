package com.example.projecttodo.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.Model.Task;
import com.example.projecttodo.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.chip.Chip;

public class TaskAdapter extends FirebaseRecyclerAdapter<Task, TaskAdapter.TaskViewHolder> {

    public TaskAdapter(@NonNull FirebaseRecyclerOptions<Task> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull TaskViewHolder holder, int position, @NonNull Task model) {
        holder.taskTitleTextView.setText(model.getTitle());
        holder.taskGroupChip.setText(model.getGroup());
        holder.taskDeadlineTextView.setText(model.getDeadline());
        holder.taskCheckbox.setChecked(model.isCompleted());
        holder.reminderTextView.setText(model.getReminder());

        // Handle checkbox click
        holder.taskCheckbox.setOnClickListener(v -> {
            boolean isChecked = holder.taskCheckbox.isChecked();
            getRef(position).child("completed").setValue(isChecked);
        });

        // Update UI based on completed status
        if (model.isCompleted()) {
            holder.itemView.setAlpha(0.6f);
            holder.taskTitleTextView.setPaintFlags(holder.taskTitleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.taskTitleTextView.setPaintFlags(holder.taskTitleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);

        return new TaskViewHolder(view);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCheckbox;
        TextView taskTitleTextView;
        Chip taskGroupChip;
        TextView taskDeadlineTextView;
        TextView reminderTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            taskCheckbox = itemView.findViewById(R.id.taskCheckbox);
            taskTitleTextView = itemView.findViewById(R.id.taskTitleTextView);
            taskGroupChip = itemView.findViewById(R.id.taskGroupChip);
            taskDeadlineTextView = itemView.findViewById(R.id.taskDeadlineTextView);
            reminderTextView = itemView.findViewById(R.id.reminderTextView);
        }
    }
}
