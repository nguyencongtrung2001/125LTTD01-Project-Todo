package com.example.projecttodo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvAddGroup, tvDeadline;
    private LinearLayout layoutDeadline;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private View colorYellow, colorGreen, colorTeal, colorBlue, colorLightBlue, colorOrange, colorPurple, colorPink;
    private Button btnCancel, btnCreate;

    private Calendar deadlineCalendar;
    private String selectedColor = "#CCDD22"; // Default yellow
    private String selectedReminder = "10p"; // Default 10p
    private String selectedPriority = "Cao"; // Default high

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        setupSpinner();
        setupListeners();
        deadlineCalendar = Calendar.getInstance();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        tvAddGroup = findViewById(R.id.tvAddGroup);
        tvDeadline = findViewById(R.id.tvDeadline);
        layoutDeadline = findViewById(R.id.layoutDeadline);
        chipGroupReminder = findViewById(R.id.chipGroupReminder);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);

        colorYellow = findViewById(R.id.colorYellow);
        colorGreen = findViewById(R.id.colorGreen);
        colorTeal = findViewById(R.id.colorTeal);
        colorBlue = findViewById(R.id.colorBlue);
        colorLightBlue = findViewById(R.id.colorLightBlue);
        colorOrange = findViewById(R.id.colorOrange);
        colorPurple = findViewById(R.id.colorPurple);
        colorPink = findViewById(R.id.colorPink);

        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
    }

    private void setupSpinner() {
        ArrayList<String> groups = new ArrayList<>();
        groups.add("Học tập");
        groups.add("Công việc");
        groups.add("Cá nhân");
        groups.add("Thể thao");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, groups);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(adapter);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        layoutDeadline.setOnClickListener(v -> showDateTimePicker());

        // Reminder chips
        chipGroupReminder.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipNone) {
                selectedReminder = "Không";
            } else if (checkedId == R.id.chip5p) {
                selectedReminder = "5p";
            } else if (checkedId == R.id.chip10p) {
                selectedReminder = "10p";
            } else if (checkedId == R.id.chip1h) {
                selectedReminder = "1h";
            } else if (checkedId == R.id.chipCustom) {
                selectedReminder = "Tùy chỉnh";
            }
        });

        // Priority chips
        chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipLow) {
                selectedPriority = "Thấp";
            } else if (checkedId == R.id.chipMedium) {
                selectedPriority = "Trung bình";
            } else if (checkedId == R.id.chipHigh) {
                selectedPriority = "Cao";
            }
        });

        // Color selection
        colorYellow.setOnClickListener(v -> selectColor("#CCDD22"));
        colorGreen.setOnClickListener(v -> selectColor("#66DD66"));
        colorTeal.setOnClickListener(v -> selectColor("#22DDBB"));
        colorBlue.setOnClickListener(v -> selectColor("#4499DD"));
        colorLightBlue.setOnClickListener(v -> selectColor("#44BBEE"));
        colorOrange.setOnClickListener(v -> selectColor("#EE9944"));
        colorPurple.setOnClickListener(v -> selectColor("#BB66DD"));
        colorPink.setOnClickListener(v -> selectColor("#EE5599"));

        // Open Select Group Dialog when clicking "Tạo nhóm mới"
        tvAddGroup.setOnClickListener(v -> showSelectGroupDialog());

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> createTask());
    }

    private void showSelectGroupDialog() {
        SelectGroupDialog dialog = new SelectGroupDialog(this, new SelectGroupDialog.OnGroupSelectedListener() {
            @Override
            public void onGroupSelected(String groupName) {
                // Update spinner with selected group
                Toast.makeText(AddTaskActivity.this, "Đã chọn nhóm: " + groupName, Toast.LENGTH_SHORT).show();
                // TODO: Add selected group to spinner
            }

            @Override
            public void onCreateNewGroup() {
                showCreateGroupDialog();
            }
        });
        dialog.show();
    }

    private void showCreateGroupDialog() {
        CreateGroupDialog dialog = new CreateGroupDialog(this, new CreateGroupDialog.OnGroupCreatedListener() {
            @Override
            public void onGroupCreated(String groupName, String color) {
                // Add new group to spinner
                Toast.makeText(AddTaskActivity.this,
                        "Đã tạo nhóm mới: " + groupName + " với màu " + color,
                        Toast.LENGTH_SHORT).show();
                // TODO: Add new group to database and refresh spinner
            }
        });
        dialog.show();
    }

    private void showDateTimePicker() {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    deadlineCalendar.set(Calendar.YEAR, year);
                    deadlineCalendar.set(Calendar.MONTH, month);
                    deadlineCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    // Show time picker after date is selected
                    TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                            (timeView, hourOfDay, minute) -> {
                                deadlineCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                deadlineCalendar.set(Calendar.MINUTE, minute);

                                updateDeadlineText();
                            },
                            calendar.get(Calendar.HOUR_OF_DAY),
                            calendar.get(Calendar.MINUTE),
                            true);
                    timePickerDialog.show();
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDeadlineText() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
        tvDeadline.setText(sdf.format(deadlineCalendar.getTime()));
    }

    private void selectColor(String color) {
        selectedColor = color;
        Toast.makeText(this, "Đã chọn màu", Toast.LENGTH_SHORT).show();
        // TODO: Update UI to show selected color
    }

    private void createTask() {
        String title = etTaskTitle.getText().toString().trim();
        String description = etTaskDescription.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_task_title, Toast.LENGTH_SHORT).show();
            return;
        }

        String group = spinnerGroup.getSelectedItem().toString();

        // TODO: Save task to database or send to backend

        Toast.makeText(this, R.string.task_created_success, Toast.LENGTH_SHORT).show();
        finish();
    }
}