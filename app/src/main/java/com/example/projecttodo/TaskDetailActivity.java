package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.*;

public class TaskDetailActivity extends AppCompatActivity {
    private TextView titleTextView, deadlineTextView, descriptionTextView, statusTextView;
    private DatabaseReference taskRef;
    private Task task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initViews();

        // NHẬN DỮ LIỆU TỪ INTENT
        if (getIntent().hasExtra("TASK_OBJECT")) {
            task = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
            if (task != null) {
                // Hiển thị tạm thời từ intent
                updateUI(task);

                // Kết nối Firebase để cập nhật Realtime
                String userId = getSharedPreferences("user_session", MODE_PRIVATE).getString("user_id", "");
                taskRef = FirebaseDatabase.getInstance().getReference("users")
                        .child(userId).child("tasks").child(task.getTaskId());

                loadTaskDetail(); // Theo dõi thay đổi từ Firebase
            }
        } else {
            Toast.makeText(this, "Không tìm thấy dữ liệu Task", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadTaskDetail() {
        taskRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Task updatedTask = snapshot.getValue(Task.class);
                    if (updatedTask != null) updateUI(updatedTask);
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void updateUI(Task currentTask) {
        titleTextView.setText(currentTask.getTitle());
        descriptionTextView.setText(currentTask.getDescription());
        deadlineTextView.setText(currentTask.getDeadline());

        if (currentTask.isCompleted()) {
            statusTextView.setText("HOÀN THÀNH");
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            statusTextView.setText("ĐANG THỰC HIỆN");
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private void initViews() {
        titleTextView = findViewById(R.id.detailTitle);
        deadlineTextView = findViewById(R.id.detailDeadline);
        descriptionTextView = findViewById(R.id.detailDescription);
        statusTextView = findViewById(R.id.detailStatus);
    }
}