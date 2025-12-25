package com.example.projecttodo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.*;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTitle, etDescription;
    private TextView tvDeadline;
    private Spinner spinnerGroup;
    private Button btnSave, btnCancel;

    private boolean isEditMode = false;
    private Task editingTask;

    private DatabaseReference db;
    private Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        db = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadGroups();
        checkEditMode();
        setupListeners();
    }

    private void initViews() {
        etTitle = findViewById(R.id.etTaskTitle);
        etDescription = findViewById(R.id.etTaskDescription);
        tvDeadline = findViewById(R.id.tvDeadline);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        btnSave = findViewById(R.id.btnCreate);
        btnCancel = findViewById(R.id.btnCancel);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("TASK_OBJECT")) {
            isEditMode = true;
            editingTask = (Task) getIntent().getSerializableExtra("TASK_OBJECT");

            etTitle.setText(editingTask.getTitle());
            etDescription.setText(editingTask.getDescription());
            tvDeadline.setText(editingTask.getDeadline());

            btnSave.setText("Cập nhật Task");
        }
    }

    private void setupListeners() {
        tvDeadline.setOnClickListener(v -> pickDateTime());
        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            if (isEditMode) updateTask();
            else createTask();
        });
    }

    private void createTask() {
        String userId = getUserId();
        if (userId == null) return;

        String id = db.child("users").child(userId).child("tasks").push().getKey();

        Map<String, Object> map = buildTaskMap(false);
        map.put("taskId", id);

        db.child("users").child(userId).child("tasks").child(id)
                .setValue(map)
                .addOnSuccessListener(a -> finish());
    }

    private void updateTask() {
        String userId = getUserId();
        if (userId == null) return;

        db.child("users")
                .child(userId)
                .child("tasks")
                .child(editingTask.getTaskId())
                .updateChildren(buildTaskMap(editingTask.isCompleted()))
                .addOnSuccessListener(a -> {
                    setResult(RESULT_OK);
                    finish();
                });
    }

    private Map<String, Object> buildTaskMap(boolean completed) {
        Map<String, Object> map = new HashMap<>();
        map.put("title", etTitle.getText().toString().trim());
        map.put("description", etDescription.getText().toString().trim());
        map.put("deadline", tvDeadline.getText().toString());
        map.put("Group", spinnerGroup.getSelectedItem().toString());
        map.put("completed", completed);
        map.put("priority", "Thấp");
        return map;
    }

    private void pickDateTime() {
        Calendar now = Calendar.getInstance();
        new DatePickerDialog(this, (v, y, m, d) -> {
            calendar.set(y, m, d);
            new TimePickerDialog(this, (v2, h, min) -> {
                calendar.set(Calendar.HOUR_OF_DAY, h);
                calendar.set(Calendar.MINUTE, min);
                tvDeadline.setText(
                        new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US)
                                .format(calendar.getTime())
                );
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show();
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void loadGroups() {
        List<String> groups = Arrays.asList("Công việc", "Học tập", "Cá nhân");
        spinnerGroup.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups)
        );
    }

    private String getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        return prefs.getString("user_id", null);
    }
}
