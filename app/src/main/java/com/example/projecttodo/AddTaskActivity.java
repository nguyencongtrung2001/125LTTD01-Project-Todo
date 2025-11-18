package com.example.projecttodo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    // Sử dụng enum để quản lý độ ưu tiên
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }

    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvDeadline, tvAddGroup;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private LinearLayout layoutDeadline, layoutTaskColors;
    private Button btnCancel, btnCreate;
    private ImageButton btnBack;

    // Dữ liệu
    private List<String> groupList = new ArrayList<>();
    private ArrayAdapter<String> groupAdapter;
    private int[] colors = {Color.RED, Color.rgb(255, 165, 0), Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
    private List<Button> colorButtons = new ArrayList<>();
    private int selectedColorIndex = -1;
    private Calendar selectedDateTime = Calendar.getInstance();
    private Priority selectedPriority; // Sử dụng enum

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        setupListeners();
        populateColors();
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

        // Setup Spinner
        groupList.add("Default");
        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupList);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);

        // Default selections
        chipGroupReminder.check(R.id.chipNone);
    }

    private void populateColors() {
        layoutTaskColors.removeAllViews();
        colorButtons.clear();

        for (int i = 0; i < colors.length; i++) {
            final int color = colors[i];
            final int index = i;

            Button colorBtn = new Button(this);
            colorBtn.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
            colorBtn.setPadding(0, 0, 0, 0);

            ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, 30f)
                    .build();
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            colorBtn.setBackground(drawable);

            colorBtn.setText("");
            colorBtn.setId(View.generateViewId());

            colorBtn.setOnClickListener(v -> {
                if (selectedColorIndex != -1) {
                    GradientDrawable oldDrawable = (GradientDrawable) colorButtons.get(selectedColorIndex).getBackground();
                    oldDrawable.setStroke(0, Color.TRANSPARENT);
                }
                selectedColorIndex = index;
                GradientDrawable newDrawable = (GradientDrawable) colorBtn.getBackground();
                newDrawable.setStroke(3, Color.WHITE);
                Toast.makeText(this, "Chọn màu " + (index + 1), Toast.LENGTH_SHORT).show();
            });

            layoutTaskColors.addView(colorBtn);
            colorButtons.add(colorBtn);
        }
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        layoutDeadline.setOnClickListener(v -> showDateTimePicker());
        tvAddGroup.setOnClickListener(v -> showCreateGroupDialog());

        chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            int colorPrimary = ContextCompat.getColor(this, R.color.button_purple);
            int colorOnPrimary = ContextCompat.getColor(this, R.color.white);
            int colorSurfaceVariant = ContextCompat.getColor(this, R.color.colorChipBackground);
            int colorOnSurface = ContextCompat.getColor(this, R.color.black);

            for (int i = 0; i < group.getChildCount(); i++) {
                Chip chip = (Chip) group.getChildAt(i);
                if (chip.getId() == checkedId) {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(colorPrimary));
                    chip.setTextColor(colorOnPrimary);
                } else {
                    chip.setChipBackgroundColor(ColorStateList.valueOf(colorSurfaceVariant));
                    chip.setTextColor(colorOnSurface);
                }
            }

            if (checkedId == R.id.chipLow) {
                selectedPriority = Priority.LOW;
            } else if (checkedId == R.id.chipMedium) {
                selectedPriority = Priority.MEDIUM;
            } else if (checkedId == R.id.chipHigh) {
                selectedPriority = Priority.HIGH;
            }

            if (selectedPriority != null) {
                Toast.makeText(this, "Độ ưu tiên: " + selectedPriority.name(), Toast.LENGTH_SHORT).show();
            }
        });

        chipGroupPriority.check(R.id.chipLow);

        Chip chipCustom = findViewById(R.id.chipCustom);
        chipCustom.setOnClickListener(v -> showCustomReminderDialog());

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_task_title), Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Tạo task: '" + title + "' với độ ưu tiên: " + selectedPriority.name(), Toast.LENGTH_LONG).show();
            finish();
        });
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                updateDeadlineText();
            }, currentDate.get(Calendar.HOUR_OF_DAY), currentDate.get(Calendar.MINUTE), false).show();
        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
    }

    private void updateDeadlineText() {
        String myFormat = "dd/MM/yyyy HH:mm";
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(myFormat, Locale.US);
        tvDeadline.setText(sdf.format(selectedDateTime.getTime()));
    }

    private void showCustomReminderDialog() {
        CustomReminderDialog dialog = new CustomReminderDialog(this, (days, hours, minutes) -> {
            Chip chipCustom = findViewById(R.id.chipCustom);
            String customText = String.format("%d ngày %d giờ %d phút", days, hours, minutes);
            chipCustom.setText("Tùy chỉnh: " + customText);
            chipGroupReminder.check(R.id.chipCustom);
        });
        dialog.show();
    }

    private void showCreateGroupDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_create_group);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);

        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etGroupName = dialog.findViewById(R.id.etGroupName);
        Button btnCancelCreate = dialog.findViewById(R.id.btnCancelCreate);
        Button btnCreateGroup = dialog.findViewById(R.id.btnCreateGroup);

        btnCancelCreate.setOnClickListener(v -> dialog.dismiss());

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();
            if (groupName.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
            } else {
                groupList.add(groupName);
                groupAdapter.notifyDataSetChanged();
                spinnerGroup.setSelection(groupAdapter.getPosition(groupName));
                Toast.makeText(AddTaskActivity.this, "Đã tạo nhóm: " + groupName, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
