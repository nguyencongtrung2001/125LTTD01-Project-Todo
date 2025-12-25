package com.example.projecttodo;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AddTaskActivity extends AppCompatActivity {

    // Sử dụng enum để quản lý độ ưu tiên
    public enum Priority {
        LOW,
        MEDIUM,
        HIGH
    }
    private boolean isEditMode = false;
    private String editTaskId = null; // Lưu lại ID để cập nhật đúng vị trí trên Firebase
    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvDeadline, tvAddGroup;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private LinearLayout layoutDeadline, layoutTaskColors;
    private Button btnCancel, btnCreate;
    private ImageButton btnBack;

    // Firebase
    private DatabaseReference mDatabase;

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

        // Khởi tạo Firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadUserGroups();
        setupListeners();
        populateColors();
        // ADD: kiểm tra có phải chế độ chỉnh sửa không
        if (getIntent().hasExtra("taskId")) {
            isEditMode = true;
            editTaskId = getIntent().getStringExtra("taskId");

            etTaskTitle.setText(getIntent().getStringExtra("title"));
            etTaskDescription.setText(getIntent().getStringExtra("description"));
            tvDeadline.setText(getIntent().getStringExtra("deadline"));

            btnCreate.setText("Cập nhật");
        }
        if (getIntent().hasExtra("taskId")) {
            isEditMode = true;
            editTaskId = getIntent().getStringExtra("taskId");

            etTaskTitle.setText(getIntent().getStringExtra("title"));
            etTaskDescription.setText(getIntent().getStringExtra("description"));
            tvDeadline.setText(getIntent().getStringExtra("deadline"));

            // 🔥 set spinner group
            String group = getIntent().getStringExtra("group");
            if (group != null) {
                spinnerGroup.post(() -> {
                    int pos = groupAdapter.getPosition(group);
                    if (pos >= 0) spinnerGroup.setSelection(pos);
                });
            }

            // 🔥 set priority chip
            String priority = getIntent().getStringExtra("priority");
            if ("Cao".equals(priority)) {
                chipGroupPriority.check(R.id.chipHigh);
            } else if ("Trung bình".equals(priority)) {
                chipGroupPriority.check(R.id.chipMedium);
            } else {
                chipGroupPriority.check(R.id.chipLow);
            }

            btnCreate.setText("Cập nhật");
        }


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
        groupAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groupList);
        groupAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGroup.setAdapter(groupAdapter);
    }

    private void loadUserGroups() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Sử dụng Set để lưu trữ các nhóm duy nhất để tránh trùng lặp
        final Set<String> uniqueGroups = new HashSet<>();
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        // 1. Tìm nạp từ users/{userId}/groups (các nhóm được xác định trước)
        userRef.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getValue(String.class);
                    if (groupName != null && !groupName.isEmpty()) {
                        uniqueGroups.add(groupName);
                    }
                }

                // 2. Tìm nạp từ users/{userId}/tasks để nhận các nhóm đặc biệt
                userRef.child("tasks").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot tasksSnapshot) {
                        for (DataSnapshot taskSnapshot : tasksSnapshot.getChildren()) {
                            String taskGroup = taskSnapshot.child("Group").getValue(String.class);
                            if (taskGroup != null && !taskGroup.isEmpty()) {
                                uniqueGroups.add(taskGroup);
                            }
                        }

                        // 3. Cập nhật bộ điều hợp với danh sách kết hợp, duy nhất
                        groupList.clear();
                        groupList.addAll(uniqueGroups);
                        groupAdapter.notifyDataSetChanged();

                        if (groupList.isEmpty()) {
                            Toast.makeText(AddTaskActivity.this, "Bạn chưa có nhóm nào, hãy tạo một nhóm mới!", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(AddTaskActivity.this, "Lỗi khi tải nhóm từ các công việc đã có", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTaskActivity.this, "Lỗi khi tải danh sách nhóm", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void openEdit(Task task) {
        Intent intent = new Intent(this, AddTaskActivity.class);
        intent.putExtra("taskId", task.getTaskId());
        intent.putExtra("title", task.getTitle());
        intent.putExtra("description", task.getDescription());
        intent.putExtra("deadline", task.getDeadline());
        intent.putExtra("group", task.getGroup());
        intent.putExtra("priority", task.getPriority());
        startActivity(intent);
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

        chipGroupReminder.setOnCheckedChangeListener((group, checkedId) -> {
            int colorPrimary = ContextCompat.getColor(this, R.color.button_purple);
            int colorOnPrimary = Color.WHITE;
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
        });

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
        });

        chipGroupReminder.check(R.id.chipNone);
        chipGroupPriority.check(R.id.chipLow);

        Chip chipCustom = findViewById(R.id.chipCustom);
        chipCustom.setOnClickListener(v -> showCustomReminderDialog());

        btnCancel.setOnClickListener(v -> finish());

        btnCreate.setOnClickListener(v -> {
            if (isEditMode) {
                updateTask();   // ADD
            } else {
                createTask();
            }
        });

    }
    // ADD: cập nhật công việc
    private void updateTask() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId == null || editTaskId == null) {
            Toast.makeText(this, "Không tìm thấy công việc để cập nhật", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("title", etTaskTitle.getText().toString().trim());
        updates.put("description", etTaskDescription.getText().toString().trim());
        updates.put("deadline", tvDeadline.getText().toString());
        updates.put("Group", spinnerGroup.getSelectedItem().toString());

        int checkedPriorityId = chipGroupPriority.getCheckedChipId();
        if (checkedPriorityId == R.id.chipHigh) {
            updates.put("priority", "Cao");
        } else if (checkedPriorityId == R.id.chipMedium) {
            updates.put("priority", "Trung bình");
        } else {
            updates.put("priority", "Thấp");
        }

        mDatabase.child("users")
                .child(userId)
                .child("tasks")
                .child(editTaskId)
                .updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Lỗi cập nhật: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }


    private void createTask() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để tạo công việc", Toast.LENGTH_SHORT).show();
            finish(); // Đóng activity nếu không có user id
            return;
        }

        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_empty_task_title), Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerGroup.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng tạo một nhóm trước", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etTaskDescription.getText().toString().trim();
        String group = spinnerGroup.getSelectedItem().toString();
        String deadline = tvDeadline.getText().toString();
        String priority = "Thấp"; // Default
        int checkedPriorityId = chipGroupPriority.getCheckedChipId();
        if (checkedPriorityId == R.id.chipHigh) {
            priority = "Cao";
        } else if (checkedPriorityId == R.id.chipMedium) {
            priority = "Trung bình";
        }

        String taskId = mDatabase.child("users").child(userId).child("tasks").push().getKey();
        if (taskId == null) {
            Toast.makeText(this, "Lỗi khi tạo mã công việc", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> taskValues = new HashMap<>();
        taskValues.put("title", title);
        taskValues.put("description", description);
        taskValues.put("Group", group);
        taskValues.put("deadline", deadline);
        taskValues.put("priority", priority);
        taskValues.put("completed", false);
        taskValues.put("createAt", getCurrentTime());

        mDatabase.child("users").child(userId).child("tasks").child(taskId).setValue(taskValues)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(AddTaskActivity.this, "Tạo thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(AddTaskActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US).format(Calendar.getInstance().getTime());
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
        String myFormat = "HH:mm dd/MM/yyyy";
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
                return;
            }

            SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
            String userId = prefs.getString("user_id", null);

            if (userId == null || userId.isEmpty()) {
                Toast.makeText(AddTaskActivity.this, "Vui lòng đăng nhập để tạo nhóm", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                return;
            }

            DatabaseReference groupsRef = mDatabase.child("users").child(userId).child("groups");

            // Đọc số lượng group hiện tại để tạo key tăng dần
            groupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                    long count = groupsSnapshot.getChildrenCount();
                    String formattedCount = String.format("%03d", count + 1);
                    String groupId = "group_" + formattedCount;

                    groupsRef.child(groupId).setValue(groupName)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AddTaskActivity.this, "Đã tạo nhóm: " + groupName, Toast.LENGTH_SHORT).show();
                                groupList.add(groupName);
                                groupAdapter.notifyDataSetChanged();
                                spinnerGroup.setSelection(groupAdapter.getPosition(groupName));
                                dialog.dismiss();
                            })
                            .addOnFailureListener(e -> Toast.makeText(AddTaskActivity.this, "Lỗi khi tạo nhóm: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(AddTaskActivity.this, "Lỗi đọc dữ liệu groups: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
