// File: com.example.projecttodo.TaskDetailActivity.java

package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint; // Import mới cho gạch ngang
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.firebase.database.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TaskDetailActivity extends AppCompatActivity {

    private static final String TAG = "TaskDetailActivity";

    private TextView titleTextView, deadlineTextView, descriptionTextView, statusTextView;
    private Button btnEditTask, btnDeleteTask;
    private DatabaseReference taskRef;
    private Task task;
    private String taskId;
    private String userId;
    private final SimpleDateFormat DEADLINE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initViews();

        taskId = getIntent().getStringExtra("TASK_OBJECT"); // Lấy taskId (nếu dùng intent.putExtra("TASK_OBJECT", task.getTaskId());)

        // Nếu dùng object Serializable (như trong TaskListFragment ở trên)
        if (getIntent().hasExtra("TASK_OBJECT")) {
            task = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
            if (task != null) {
                taskId = task.getTaskId();
            }
        }

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getString("user_id", "");

        if (taskId == null || userId.isEmpty()) {
            Toast.makeText(this, "Lỗi: Dữ liệu Task không hợp lệ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Trỏ đến TaskRef
        taskRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("tasks").child(taskId);

        // Gọi loadTaskDetail trong onCreate
        loadTaskDetail();
    }

    private void initViews() {
        titleTextView = findViewById(R.id.detailTitle);
        deadlineTextView = findViewById(R.id.detailDeadline);
        descriptionTextView = findViewById(R.id.detailDescription);
        statusTextView = findViewById(R.id.detailStatus);
        btnEditTask = findViewById(R.id.btnEditTask);
        btnDeleteTask = findViewById(R.id.btnDeleteTask);
    }

    // Sử dụng ValueEventListener để theo dõi và tự cập nhật
    private void loadTaskDetail() {
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(TaskDetailActivity.this, "Task đã bị xóa.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                task = snapshot.getValue(Task.class);
                if (task != null) {
                    updateUI(task);
                    setupActionListeners(task);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Lỗi tải chi tiết Firebase: " + error.getMessage());
                Toast.makeText(TaskDetailActivity.this, "Lỗi tải chi tiết: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUI(Task currentTask) {
        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());

        boolean isOverdue = false;

        // 1. Cập nhật Deadline và kiểm tra Quá hạn
        try {
            Date now = new Date();
            Date deadline = DEADLINE_FORMAT.parse(currentTask.getDeadline());

            String formattedDeadline = DEADLINE_FORMAT.format(deadline);
            deadlineTextView.setText(formattedDeadline);

            if (!currentTask.isCompleted() && deadline.before(now)) {
                isOverdue = true;
                // Màu đỏ đậm cho Quá hạn
                deadlineTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
            } else {
                deadlineTextView.setTextColor(ContextCompat.getColor(this, android.R.color.black));
            }
        } catch (ParseException e) {
            Log.e(TAG, "Lỗi phân tích cú pháp Deadline", e);
            deadlineTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_orange_dark));
        }

        // 2. Cập nhật Trạng thái và màu sắc
        if (currentTask.isCompleted()) {
            statusTextView.setText("ĐÃ HOÀN THÀNH");
            statusTextView.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark));
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG); // Gạch ngang
        } else {
            statusTextView.setText(isOverdue ? "QUÁ HẠN" : "ĐANG CHỜ");
            statusTextView.setTextColor(ContextCompat.getColor(this, isOverdue ? android.R.color.holo_red_dark : android.R.color.holo_orange_dark));
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)); // Bỏ gạch ngang
        }

        // 3. Ẩn/hiện nút Edit
        checkEditPermission(currentTask, isOverdue);
    }

    private void checkEditPermission(Task currentTask, boolean isOverdue) {
        // Cho phép Edit nếu CHƯA HOÀN THÀNH VÀ CHƯA QUÁ HẠN
        boolean canEdit = !currentTask.isCompleted() && !isOverdue;

        btnEditTask.setVisibility(canEdit ? View.VISIBLE : View.GONE);
    }

    private void setupActionListeners(Task currentTask) {
        // Listener cho Edit (chỉ đặt khi nút hiển thị)
        if (btnEditTask.getVisibility() == View.VISIBLE) {
            btnEditTask.setOnClickListener(v -> {
                Intent intent = new Intent(TaskDetailActivity.this, AddTaskActivity.class);
                // Truyền taskId để màn hình AddTask biết là đang EDIT
                intent.putExtra("TASK_ID_TO_EDIT", currentTask.getTaskId());
                startActivity(intent);
            });
        }

        // Listener cho Delete
        btnDeleteTask.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(TaskDetailActivity.this)
                .setTitle("Xác nhận")
                .setMessage("Bạn có chắc muốn xóa task này không?")
                .setPositiveButton("CÓ, XÓA", (dialog, which) -> {
                    taskRef.removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // finish() sẽ được gọi tự động khi loadTaskDetail phát hiện snapshot không tồn tại
                        } else {
                            Toast.makeText(TaskDetailActivity.this, "Lỗi xóa Task.", Toast.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton("HỦY", null)
                .show();
    }
}