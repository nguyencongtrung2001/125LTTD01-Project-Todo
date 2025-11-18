package com.example.projecttodo; // SỬA: Đảm bảo package là 'adapter'

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.R;
 // THÊM/SỬA: Import lớp Task từ gói model

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private Context context;

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    public void updateTasks(List<Task> newTasks) {
        this.taskList.clear();
        this.taskList.addAll(newTasks);
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
        Task task = taskList.get(position);
        holder.titleTextView.setText(task.getTitle());

        // SỬA LỖI: Gọi getDeadline() (D hoa)
        holder.deadlineTextView.setText(task.getdeadline());

        // Thiết lập giao diện khác dựa trên task.isCompleted() hoặc task.getPriority()
        // ... (Logic màu sắc không đổi)
        if (task.isCompleted()) {
            holder.titleTextView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
        } else {
            holder.titleTextView.setTextColor(context.getResources().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView deadlineTextView;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.taskTitle);
            deadlineTextView = itemView.findViewById(R.id.taskdeadline);
        }
    }
}