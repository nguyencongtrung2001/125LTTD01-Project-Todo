package com.example.projecttodo;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private final Context context;
    private List<Task> tasks;
    private final TaskItemClickListener listener;
    private final String userId;
    private boolean isSelecting = false;
    private final List<String> selectedTaskIds = new ArrayList<>();
    private final DatabaseReference databaseReference;

    public TaskAdapter(Context context, List<Task> tasks, String userId, TaskItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.userId = userId;
        this.listener = listener;
        this.databaseReference = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("tasks");
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

        // Hiển thị dữ liệu
        holder.tvTitle.setText(task.getTitle());
        holder.tvDeadline.setText(task.getDeadline());
        holder.tvGroup.setText(task.getGroup());

        // Xử lý UI Hoàn thành (Gạch ngang + mờ)
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1.0f);
        }

        // Xử lý Icon chọn (Multi-select)
        holder.ivSelect.setImageResource(selectedTaskIds.contains(task.getTaskId())
                ? R.drawable.ic_circle_checked : R.drawable.ic_circle_unchecked);

        // --- SỰ KIỆN CLICK VÀO ITEM ---
        holder.itemView.setOnClickListener(v -> {
            if (isSelecting) {
                toggleSelect(task.getTaskId());
            } else {
                // ĐÂY LÀ CHỖ MỞ CHI TIẾT
                listener.onTaskClick(task);
            }
        });

        // --- LONG CLICK ĐỂ BẬT CHẾ ĐỘ CHỌN ---
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelecting) {
                toggleSelect(task.getTaskId());
                return true;
            }
            return false;
        });
    }

    private void toggleSelect(String taskId) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId);
        } else {
            selectedTaskIds.add(taskId);
        }

        isSelecting = !selectedTaskIds.isEmpty();
        listener.onSelectionModeChange(isSelecting);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedTaskIds.clear();
        isSelecting = false;
        listener.onSelectionModeChange(false);
        notifyDataSetChanged();
    }

    public List<String> getSelectedTaskIds() { return selectedTaskIds; }
    public boolean isSelectingMode() { return isSelecting; }
    @Override
    public int getItemCount() { return tasks.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView ivSelect;
        TextView tvTitle, tvDeadline, tvGroup;
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSelect = itemView.findViewById(R.id.ivSelect);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvGroup = itemView.findViewById(R.id.tvTaskGroup);
        }
    }
}