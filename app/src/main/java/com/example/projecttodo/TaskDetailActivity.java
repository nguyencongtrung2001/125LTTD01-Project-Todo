package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TaskDetailActivity extends AppCompatActivity {

    private static final int REQ_EDIT = 1001;

    private TextView tvTitle, tvDeadline, tvDesc, tvStatus;
    private Button btnEdit, btnComplete;

    private Task task;
    private DatabaseReference taskRef;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        initViews();
        getUserId();
        getTask();
        setupFirebase();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.detailTitle);
        tvDeadline = findViewById(R.id.detailDeadline);
        tvDesc = findViewById(R.id.detailDescription);
        tvStatus = findViewById(R.id.detailStatus);
        btnEdit = findViewById(R.id.btnEdit);
        btnComplete = findViewById(R.id.btnToggleComplete);
    }

    private void getTask() {
        task = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
    }

    private void setupFirebase() {
        taskRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("tasks")
                .child(task.getTaskId());
    }

    private void setupListeners() {
        btnEdit.setOnClickListener(v -> {
            Intent i = new Intent(this, AddTaskActivity.class);
            i.putExtra("TASK_OBJECT", task);
            startActivityForResult(i, REQ_EDIT);
        });

        btnComplete.setOnClickListener(v ->
                taskRef.child("completed").setValue(true)
                        .addOnSuccessListener(a -> reload())
        );
    }

    private void reload() {
        taskRef.get().addOnSuccessListener(snap -> {
            task = snap.getValue(Task.class);
            updateUI();
        });
    }

    private void updateUI() {
        tvTitle.setText(task.getTitle());
        tvDeadline.setText(task.getDeadline());
        tvDesc.setText(task.getDescription());

        if (task.isCompleted()) {
            tvStatus.setText("HOÀN THÀNH");
            tvStatus.setTextColor(Color.GREEN);
            btnEdit.setVisibility(View.GONE);
            btnComplete.setVisibility(View.GONE);
        } else {
            tvStatus.setText("ĐANG THỰC HIỆN");
            tvStatus.setTextColor(Color.RED);
        }
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getString("user_id", null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_EDIT && resultCode == RESULT_OK) reload();
    }
}
