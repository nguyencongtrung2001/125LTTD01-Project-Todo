package com.example.projecttodo;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.util.ArrayList;
import java.util.List;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvDeadline, tvAddGroup;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private LinearLayout layoutDeadline, layoutTaskColors;
    private Button btnCancel, btnCreate;
    private ImageButton btnBack;

    // Màu sắc mẫu (có thể chỉnh)
    private int[] colors = {Color.RED, Color.rgb(255, 165, 0), Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA};
    private List<Button> colorButtons = new ArrayList<>();
    private int selectedColorIndex = -1;  // -1: chưa chọn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        initViews();
        setupListeners();
        populateColors();  // Thêm danh sách màu động
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

        // Default selections
        chipGroupReminder.check(R.id.chipNone);
        chipGroupPriority.check(R.id.chipLow);

        // TODO: Setup Spinner adapter nếu có data groups
    }

    private void populateColors() {
        layoutTaskColors.removeAllViews();  // Xóa nếu có cũ
        colorButtons.clear();

        for (int i = 0; i < colors.length; i++) {
            final int color = colors[i];
            final int index = i;

            // Tạo Button tròn cho màu
            Button colorBtn = new Button(this);
            colorBtn.setLayoutParams(new LinearLayout.LayoutParams(60, 60));  // Kích thước tròn
            colorBtn.setPadding(0, 0, 0, 0);

            // Shape tròn (circular)
            ShapeAppearanceModel shapeModel = ShapeAppearanceModel.builder()
                    .setAllCorners(CornerFamily.ROUNDED, 30f)  // Bán kính tròn
                    .build();
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.OVAL);
            drawable.setColor(color);
            colorBtn.setBackground(drawable);

            // Không text, chỉ màu
            colorBtn.setText("");
            colorBtn.setId(View.generateViewId());  // ID unique

            // Listener: Chọn màu
            colorBtn.setOnClickListener(v -> {
                if (selectedColorIndex != -1) {
                    // Reset màu cũ
                    GradientDrawable oldDrawable = (GradientDrawable) colorButtons.get(selectedColorIndex).getBackground();
                    oldDrawable.setStroke(0, Color.TRANSPARENT);
                }
                // Set màu mới với viền trắng
                selectedColorIndex = index;
                GradientDrawable newDrawable = (GradientDrawable) colorBtn.getBackground();
                newDrawable.setStroke(3, Color.WHITE);  // Viền trắng 3dp
                Toast.makeText(this, "Chọn màu " + (index + 1), Toast.LENGTH_SHORT).show();
            });

            layoutTaskColors.addView(colorBtn);
            colorButtons.add(colorBtn);
        }
    }

    private void setupListeners() {
        // Nút Back
        btnBack.setOnClickListener(v -> finish());

        // Layout Deadline (click mở DatePicker)
        layoutDeadline.setOnClickListener(v -> {
            // TODO: DatePickerDialog
            Toast.makeText(this, "Chọn ngày hạn", Toast.LENGTH_SHORT).show();
        });

        // tvAddGroup
        tvAddGroup.setOnClickListener(v -> {
            // TODO: Dialog thêm group
            Toast.makeText(this, "Thêm nhóm mới", Toast.LENGTH_SHORT).show();
        });

        // Chip Custom Reminder: Hiển thị Dialog centered + dim background
        Chip chipCustom = findViewById(R.id.chipCustom);
        chipCustom.setOnClickListener(v -> showCustomReminderDialog());

        // Nút Cancel
        btnCancel.setOnClickListener(v -> finish());

        // Nút Create
        btnCreate.setOnClickListener(v -> {
            String title = etTaskTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
                return;
            }
            // TODO: Save task (bao gồm selectedColorIndex nếu cần)
            Toast.makeText(this, "Tạo task: " + title + " (Màu: " + (selectedColorIndex != -1 ? colors[selectedColorIndex] : "Mặc định"), Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showCustomReminderDialog() {
        CustomReminderDialog dialog = new CustomReminderDialog(this, new CustomReminderDialog.OnReminderSetListener() {
            @Override
            public void onReminderSet(int days, int hours, int minutes) {
                // Cập nhật chip text với thời gian custom
                Chip chipCustom = findViewById(R.id.chipCustom);
                String customText = String.format("%d ngày %d giờ %d phút", days, hours, minutes);
                chipCustom.setText("Tùy chỉnh: " + customText);
                chipGroupReminder.check(R.id.chipCustom);  // Chọn chip này
            }
        });
        dialog.show();  // Hiển thị, tự động centered + dim
    }
}