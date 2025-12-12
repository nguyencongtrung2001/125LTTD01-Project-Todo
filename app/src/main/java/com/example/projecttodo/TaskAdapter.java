// File: com.example.projecttodo.TaskAdapter.java

package com.example.projecttodo;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private static final String TAG = "TaskAdapter";
    private final Context context;
    private List<Task> tasks;
    private final TaskItemClickListener listener;

    // Sửa lỗi: Khai báo biến thành viên để lưu trữ userId
    private final String userId;

    private boolean isSelecting = false;
    private final List<String> selectedTaskIds = new ArrayList<>();

    private final DatabaseReference databaseReference;

    public TaskAdapter(Context context, List<Task> tasks, String userId, TaskItemClickListener listener) {
        this.context = context;
        this.tasks = tasks;
        this.listener = listener;

        // Gán giá trị userId vào biến thành viên của lớp
        this.userId = userId;

        // Khởi tạo DatabaseReference sử dụng biến thành viên userId đã được gán
        this.databaseReference = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(this.userId)
                .child("tasks");
    }

    public void updateTasks(List<Task> newTasks) {
        this.tasks = newTasks;
        notifyDataSetChanged();
    }

    public List<String> getSelectedTaskIds() {
        return selectedTaskIds;
    }

    public void clearSelection() {
        selectedTaskIds.clear();
        isSelecting = false;
        notifyDataSetChanged();
        listener.onSelectionModeChange(false);
    }

    public boolean isSelectingMode() {
        return isSelecting;
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

        // Xử lý UI cho Task đã hoàn thành
        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.itemView.setAlpha(0.6f);
            // Ẩn các nút hành động khi task đã hoàn thành (tùy chọn UI)
            holder.btnDeleteTaskItem.setVisibility(View.GONE);
            holder.btnCompleteTask.setVisibility(View.GONE);
        } else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.itemView.setAlpha(1f);
            holder.btnDeleteTaskItem.setVisibility(View.VISIBLE);
            holder.btnCompleteTask.setVisibility(View.VISIBLE);
        }

        // Set trạng thái chọn (trong chế độ multi-select)
        holder.ivSelect.setImageResource(selectedTaskIds.contains(task.getTaskId())
                ? R.drawable.ic_circle_checked
                : R.drawable.ic_circle_unchecked);

        // --- Sự kiện click vào biểu tượng chọn ---
        holder.ivSelect.setOnClickListener(v -> {
            toggleSelect(task.getTaskId(), holder);
        });

        // --- Sự kiện click vào Item (mở chi tiết hoặc chọn) ---
        holder.itemView.setOnClickListener(v -> {
            if (isSelecting) {
                toggleSelect(task.getTaskId(), holder);
            } else {
                listener.onTaskClick(task); // Mở TaskDetailActivity
            }
        });

        // --- Sự kiện Long click (bật chế độ chọn hàng loạt) ---
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelecting) {
                toggleSelect(task.getTaskId(), holder);
                return true;
            }
            return false;
        });

        // --- Xử lý nút Hoàn thành Task ---
        holder.btnCompleteTask.setOnClickListener(v -> {
            updateTaskCompletionStatus(task.getTaskId(), true);
        });

        // --- Xử lý nút Xóa Task ---
        holder.btnDeleteTaskItem.setOnClickListener(v -> {
            showDeleteConfirmationDialog(task);
        });
    }

    // Phương thức cập nhật trạng thái hoàn thành trên Firebase
    private void updateTaskCompletionStatus(String taskId, boolean isCompleted) {
        databaseReference.child(taskId).child("completed").setValue(isCompleted)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Task đã hoàn thành", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi cập nhật trạng thái: " + e.getMessage());
                    Toast.makeText(context, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức bật/tắt chọn một item
    private void toggleSelect(String taskId, TaskViewHolder holder) {
        if (selectedTaskIds.contains(taskId)) {
            selectedTaskIds.remove(taskId);
            holder.ivSelect.setImageResource(R.drawable.ic_circle_unchecked);
        } else {
            selectedTaskIds.add(taskId);
            holder.ivSelect.setImageResource(R.drawable.ic_circle_checked);
        }

        // Kiểm tra thay đổi chế độ chọn
        if (selectedTaskIds.isEmpty()) {
            isSelecting = false;
            listener.onSelectionModeChange(false);
        } else if (!isSelecting) {
            isSelecting = true;
            listener.onSelectionModeChange(true);
        }
    }

    // Phương thức thực hiện xóa Task trên Firebase
    private void deleteTask(Task task) {
        databaseReference.child(task.getTaskId()).removeValue()
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Task '" + task.getTitle() + "' đã bị xóa.", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Lỗi xóa Task: " + e.getMessage());
                    Toast.makeText(context, "Lỗi xóa Task: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Phương thức hiển thị hộp thoại xác nhận trước khi xóa
    private void showDeleteConfirmationDialog(Task task) {
        new android.app.AlertDialog.Builder(context)
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa task: '" + task.getTitle() + "'?")
                .setPositiveButton("CÓ, XÓA", (dialog, which) -> {
                    deleteTask(task);
                })
                .setNegativeButton("HỦY", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // ViewHolder class
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        ImageView ivSelect, btnCompleteTask, btnDeleteTaskItem;
        TextView tvTitle, tvDeadline, tvGroup;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ các thành phần từ item_task.xml
            ivSelect = itemView.findViewById(R.id.ivSelect);
            btnCompleteTask = itemView.findViewById(R.id.btnCompleteTask);
            btnDeleteTaskItem = itemView.findViewById(R.id.btnDeleteTaskItem);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDeadline = itemView.findViewById(R.id.tvTaskDeadline);
            tvGroup = itemView.findViewById(R.id.tvTaskGroup);
        }
    }
}