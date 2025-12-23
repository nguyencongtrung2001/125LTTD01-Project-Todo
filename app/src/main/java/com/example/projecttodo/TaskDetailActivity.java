package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView titleTextView, deadlineTextView, descriptionTextView, statusTextView;
    private Button btnEdit, btnToggleComplete;
    private ImageButton btnBack;
    private Task task;
    private DatabaseReference taskRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        titleTextView = findViewById(R.id.detailTitle);
        deadlineTextView = findViewById(R.id.detailDeadline);
        descriptionTextView = findViewById(R.id.detailDescription);
        statusTextView = findViewById(R.id.detailStatus);
        btnEdit = findViewById(R.id.btnEdit);
        btnToggleComplete = findViewById(R.id.btnToggleComplete);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("TASK_OBJECT")) {
            task = (Task) getIntent().getSerializableExtra("TASK_OBJECT");

            String userId = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getString("user_id", "");
            taskRef = FirebaseDatabase.getInstance().getReference("users")
                    .child(userId)
                    .child("tasks")
                    .child(task.getTaskId());

            updateUI(task);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddTaskActivity.class);
                intent.putExtra("TASK_OBJECT", task);
                startActivity(intent);
                finish(); // Kết thúc màn hình chi tiết cũ để tránh dữ liệu bị lỗi thời khi quay lại
            });

            btnToggleComplete.setOnClickListener(v -> {
                task.setCompleted(true);
                taskRef.setValue(task).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Task đã hoàn thành", Toast.LENGTH_SHORT).show();
                    updateUI(task);
                });
            });
        }
    }

    private void updateUI(Task task) {
        titleTextView.setText(task.getTitle());
        descriptionTextView.setText(task.getDescription());
        deadlineTextView.setText(task.getDeadline());

        if (task.isCompleted()) {
            statusTextView.setText("HOÀN THÀNH");
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            btnEdit.setVisibility(Button.GONE);
            btnToggleComplete.setVisibility(Button.GONE);
        } else {
            statusTextView.setText("ĐANG THỰC HIỆN");
            titleTextView.setPaintFlags(titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            btnEdit.setVisibility(Button.VISIBLE);
            btnToggleComplete.setVisibility(Button.VISIBLE);
        }
    }
}
