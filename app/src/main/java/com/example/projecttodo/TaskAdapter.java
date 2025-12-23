package com.example.projecttodo;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {
    ImageView ivSelect, btnEditOrComplete, btnDeleteTaskItem;

    private final Context context;
    private List<Task> tasks;
    private final TaskItemClickListener listener;
    private final DatabaseReference databaseReference;

    public TaskAdapter(Context context, List<Task> tasks, String userId, TaskItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;
        this.databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tasks");
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDeadline.setText(task.getDeadline());
        holder.tvGroup.setText(task.getGroup());

        if (task.isCompleted()) {
            // ===== TASK ĐÃ HOÀN THÀNH =====
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            holder.itemView.setAlpha(0.6f);
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.task_completed_bg));

            holder.ivSelect.setImageResource(R.drawable.ic_check_green);
            holder.btnEditOrComplete.setImageResource(R.drawable.ic_check_green);

            // ❌ KHÔNG CHO CLICK GÌ NỮA
            holder.ivSelect.setEnabled(false);
            holder.btnEditOrComplete.setEnabled(false);
            holder.itemView.setOnClickListener(null);

        } else {
            // ===== TASK CHƯA HOÀN THÀNH =====
            holder.tvTitle.setPaintFlags(
                    holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));

            holder.itemView.setAlpha(1f);
            holder.itemView.setBackgroundColor(Color.WHITE);

            holder.ivSelect.setImageResource(R.drawable.ic_circle_unchecked);
            holder.btnEditOrComplete.setImageResource(R.drawable.ic_edit);

            holder.ivSelect.setEnabled(true);
            holder.btnEditOrComplete.setEnabled(true);

            // ✅ CLICK CHECKBOX → HOÀN THÀNH
            holder.ivSelect.setOnClickListener(v -> {
                databaseReference.child(task.getTaskId())
                        .child("completed")
                        .setValue(true);
            });

            // ✅ CLICK EDIT
            holder.btnEditOrComplete.setOnClickListener(v ->
                    listener.onEditTask(task)
            );

            // ✅ CLICK ITEM → XEM CHI TIẾT
            holder.itemView.setOnClickListener(v ->
                    listener.onTaskClick(task)
            );
        }

        // DELETE
        holder.btnDeleteTaskItem.setOnClickListener(v ->
                databaseReference.child(task.getTaskId()).removeValue()
        );
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSelect, btnEditOrComplete, btnDeleteTaskItem;
        TextView tvTitle, tvDeadline, tvGroup;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);

            ivSelect = itemView.findViewById(R.id.ivSelect);
            btnEditOrComplete = itemView.findViewById(R.id.btnEditOrComplete);
            btnDeleteTaskItem = itemView.findViewById(R.id.btnDeleteTaskItem);

            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvGroup = itemView.findViewById(R.id.tvTaskGroup);
        }
    }

}
