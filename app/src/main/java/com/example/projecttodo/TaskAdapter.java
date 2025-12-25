package com.example.projecttodo;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
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

        holder.tvTitle.setText(task.getTitle());
        holder.tvDeadline.setText(task.getDeadline());
        holder.tvGroup.setText(task.getGroup());

        // ✅ QUAN TRỌNG: LUÔN RESET GẠCH NGANG
        holder.tvTitle.setPaintFlags(
                holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
        );

        // Gắn task cho delete
        holder.itemView.setTag(task);

        if (task.isCompleted()) {
            holder.btnCompleteTaskItem.setImageResource(R.drawable.ic_check_complete);
            holder.btnCompleteTaskItem.setEnabled(false);
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.taskCompletedBg)
            );
        } else {
            holder.btnCompleteTaskItem.setImageResource(R.drawable.ic_edit);
            holder.btnCompleteTaskItem.setEnabled(true);
            holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.taskPendingBg)
            );

            holder.btnCompleteTaskItem.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Hoàn thành Task?")
                        .setMessage("Bạn có chắc muốn đánh dấu task này là hoàn thành?")
                        .setPositiveButton("Xác nhận", (dialog, which) -> {
                            task.setCompleted(true);
                            databaseReference.child(task.getTaskId()).setValue(task);
                            notifyItemChanged(position);
                        })
                        .setNegativeButton("Hủy", null)
                        .show();
            });
        }

        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }


    @Override
    public int getItemCount() { return tasks.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        ImageView btnCompleteTaskItem;
        TextView tvTitle, tvDeadline, tvGroup;
        LinearLayout btnActionLayout;
        ImageView btnDeleteTaskItem;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            btnCompleteTaskItem = itemView.findViewById(R.id.btnCompleteTaskItem);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvGroup = itemView.findViewById(R.id.tvTaskGroup);
            btnActionLayout = itemView.findViewById(R.id.btnActionLayout);
            btnDeleteTaskItem = itemView.findViewById(R.id.btnDeleteTaskItem);

            btnDeleteTaskItem.setOnClickListener(v -> {
                // Lấy task đã gắn ở bước 1
                Task task = (Task) itemView.getTag();

                if (task != null) {
                    // Sử dụng Context từ itemView
                    Context context = itemView.getContext();

                    new AlertDialog.Builder(context)
                            .setTitle("Xóa Task?")
                            .setMessage("Bạn có chắc muốn xóa '" + task.getTitle() + "'?")
                            .setPositiveButton("Xóa", (dialog, which) -> {
                                // Gọi phương thức xóa thông qua Adapter
                                TaskAdapter adapter = (TaskAdapter) getBindingAdapter();
                                if (adapter != null) {
                                    adapter.deleteTask(task.getTaskId());
                                }
                            })
                            .setNegativeButton("Hủy", null)
                            .show();
                }
            });
        }
    }

    // Thêm hàm này vào TaskAdapter
    private void deleteTask(String taskId) {
        databaseReference.child(taskId).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Đã xóa task thành công", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Lỗi khi xóa: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
