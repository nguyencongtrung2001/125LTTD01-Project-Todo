package com.example.projecttodo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.R;
import com.example.projecttodo.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;
    private Context context;

    public TaskAdapter(Context context, List<Task> tasks) {
        this.context = context;
        this.tasks = tasks;
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = tasks.get(position);
        holder.tvTitle.setText(task.getTitle());
        holder.tvDeadline.setText("Deadline: " + task.getDeadline());
        holder.tvGroup.setText("Group: " + task.getGroup());

        // Nếu task đã hoàn thành, đổi màu text hoặc style
        if (task.isCompleted()) {
            holder.tvTitle.setAlpha(0.5f);
            holder.tvDeadline.setAlpha(0.5f);
            holder.tvGroup.setAlpha(0.5f);
        } else {
            holder.tvTitle.setAlpha(1f);
            holder.tvDeadline.setAlpha(1f);
            holder.tvGroup.setAlpha(1f);
        }
    }

    @Override
    public int getItemCount() {
        return tasks.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {

        TextView tvTitle, tvDeadline, tvGroup;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvGroup = itemView.findViewById(R.id.tvTaskGroup);
        }
    }
}
