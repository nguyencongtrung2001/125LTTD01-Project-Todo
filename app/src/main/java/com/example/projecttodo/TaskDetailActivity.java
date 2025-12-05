package com.example.projecttodo;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvTitle, tvDescription, tvDeadline, tvGroup, tvPriority, tvStatus;
    private TextView btnEdit, btnDelete;

    private String taskId;
    private String userId;

    private DatabaseReference taskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        // Nhận taskId từ Intent
        taskId = getIntent().getStringExtra("taskId");

        if (taskId == null || taskId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy task", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadUserSession();
        taskRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("tasks").child(taskId);

        initViews();
        loadTaskDetail();
        setupButtons();
    }

    private void loadUserSession() {
        userId = getSharedPreferences("user_session", MODE_PRIVATE)
                .getString("user_id", "");
    }

    private void initViews() {
        tvTitle = findViewById(R.id.detailTitle);
        tvDescription = findViewById(R.id.detailDescription);
        tvDeadline = findViewById(R.id.detailDeadline);
        tvGroup = findViewById(R.id.detailGroup);
        tvPriority = findViewById(R.id.detailPriority);
        tvStatus = findViewById(R.id.detailStatus);

        btnEdit = findViewById(R.id.btnEditTask);
        btnDelete = findViewById(R.id.btnDeleteTask);
    }

    private void loadTaskDetail() {
        taskRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(TaskDetailActivity.this, "Task không tồn tại!", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }

                Task task = snapshot.getValue(Task.class);
                if (task == null) return;

                tvTitle.setText(task.getTitle());
//                tvDescription.setText(task.getDeadline());
                tvDeadline.setText(task.getDeadline());
                tvGroup.setText(task.getGroup());

                tvStatus.setText(task.isCompleted() ? "Completed" : "Not Completed");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskDetail", "Firebase load error: " + error.getMessage());
            }
        });
    }

    private void setupButtons() {

        // ========== EDIT ==========
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(TaskDetailActivity.this, AddTaskActivity.class);
            intent.putExtra("editTaskId", taskId);
            startActivity(intent);
        });

        // ========== DELETE ==========
        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(TaskDetailActivity.this)
                    .setTitle("Xóa Task?")
                    .setMessage("Bạn có chắc muốn xóa task này không?")
                    .setPositiveButton("Xóa", (dialog, which) -> deleteTask())
                    .setNegativeButton("Hủy", null)
                    .show();
        });
    }

    private void deleteTask() {
        taskRef.removeValue()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(TaskDetailActivity.this, "Đã xóa task", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(TaskDetailActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
