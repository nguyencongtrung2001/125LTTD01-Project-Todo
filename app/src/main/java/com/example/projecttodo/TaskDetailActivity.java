package com.example.projecttodo;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
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
    private String userId;

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

        // ✅ QUAY LẠI
        btnBack.setOnClickListener(v -> finish());

        if (getIntent().hasExtra("TASK_OBJECT")) {
            task = (Task) getIntent().getSerializableExtra("TASK_OBJECT");

            userId = getSharedPreferences("user_session", MODE_PRIVATE)
                    .getString("user_id", "");

            taskRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("tasks")
                    .child(task.getTaskId());

            updateUI(task);

            btnEdit.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddTaskActivity.class);

                intent.putExtra("taskId", task.getTaskId());
                intent.putExtra("title", task.getTitle());
                intent.putExtra("description", task.getDescription());
                intent.putExtra("deadline", task.getDeadline());
                intent.putExtra("group", task.getGroup());
                intent.putExtra("priority", task.getPriority());

                startActivity(intent);
            });


            btnToggleComplete.setOnClickListener(v -> {
                task.setCompleted(true);
                taskRef.child("completed").setValue(true)
                        .addOnSuccessListener(a -> {
                            Toast.makeText(this, "Đã hoàn thành", Toast.LENGTH_SHORT).show();
                            updateUI(task);
                        });
            });
        }
    }

    private void updateUI(Task task) {
        titleTextView.setText(task.getTitle());
        descriptionTextView.setText(
                task.getDescription() == null || task.getDescription().isEmpty()
                        ? "Không có mô tả"
                        : task.getDescription()
        );
        deadlineTextView.setText(task.getDeadline());

        //
        titleTextView.setPaintFlags(
                titleTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)
        );

        if (task.isCompleted()) {
            statusTextView.setText("HOÀN THÀNH");
            statusTextView.setTextColor(Color.parseColor("#4CAF50"));
            btnEdit.setVisibility(View.GONE);
            btnToggleComplete.setVisibility(View.GONE);
        } else {
            statusTextView.setText("ĐANG THỰC HIỆN");
            statusTextView.setTextColor(Color.parseColor("#FF9800"));
            btnEdit.setVisibility(View.VISIBLE);
            btnToggleComplete.setVisibility(View.VISIBLE);
        }
    }
}
