package com.example.projecttodo;

import android.os.Bundle;
import android.view.View;
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

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvDeadline, tvAddGroup;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private LinearLayout layoutDeadline, layoutTaskColors;
    private Button btnCancel, btnCreate;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);  // Giả sử tên XML là activity_add_task.xml

        initViews();
        setupListeners();
    }

    private void initViews() {
        etTaskTitle = findViewById(R.id.etTaskTitle);
        etTaskDescription = findViewById(R.id.etTaskDescription);
        spinnerGroup = findViewById(R.id.spinnerGroup);
        tvDeadline = findViewById(R.id.tvDeadline);
        tvAddGroup = findViewById(R.id.tvAddGroup);
        chipGroupReminder = findViewById(R.id.chipGroupReminder);
        chipGroupPriority = findViewById(R.id.chipGroupPriority);
        layoutDeadline = findViewById(R.id.layoutDeadline);
        layoutTaskColors = findViewById(R.id.layoutTaskColors);
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
        btnBack = findViewById(R.id.btnBack);

        // Default: Chọn chip đầu tiên (None cho reminder, Low cho priority)
        chipGroupReminder.check(R.id.chipNone);
        chipGroupPriority.check(R.id.chipLow);

        // TODO: Setup Spinner adapter (thêm groups từ DB)
        // Ví dụ: ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
        // spinnerGroup.setAdapter(adapter);

        // TODO: Setup màu sắc cho layoutTaskColors (thêm ImageView/ColorButton động)
    }

    private void setupListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Layout Deadline (click để mở DatePicker)
        layoutDeadline.setOnClickListener(v -> {
            // TODO: Mở DatePickerDialog
            Toast.makeText(this, "Chọn ngày hạn", Toast.LENGTH_SHORT).show();
            // Ví dụ: DatePickerDialog dialog = new DatePickerDialog(this, ...);
            // dialog.show();
        });

        // tvAddGroup (click để thêm group mới)
        tvAddGroup.setOnClickListener(v -> {
            // TODO: Mở dialog thêm group
            Toast.makeText(this, "Thêm nhóm mới", Toast.LENGTH_SHORT).show();
        });

        // Nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Nút Create
        btnCreate.setOnClickListener(v -> {
            // Lấy dữ liệu form
            String title = etTaskTitle.getText().toString().trim();
            String description = etTaskDescription.getText().toString().trim();
            int selectedReminderId = chipGroupReminder.getCheckedChipId();
            int selectedPriorityId = chipGroupPriority.getCheckedChipId();
            String deadline = tvDeadline.getText().toString();

            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề task", Toast.LENGTH_SHORT).show();
                return;
            }

            // TODO: Save task vào DB (Room/SQLite), sau đó finish() và refresh list ở Fragment
            Toast.makeText(this, "Tạo task thành công: " + title, Toast.LENGTH_SHORT).show();
            finish();  // Quay về TaskListFragment
        });
    }
}