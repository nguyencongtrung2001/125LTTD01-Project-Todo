package com.example.projecttodo;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class AddTaskActivity extends AppCompatActivity implements GroupSpinnerAdapter.GroupActionListener {

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private boolean isEditMode = false;
    private String editTaskId = null;
    private EditText etTaskTitle, etTaskDescription;
    private Spinner spinnerGroup;
    private TextView tvDeadline, tvAddGroup;
    private ChipGroup chipGroupReminder, chipGroupPriority;
    private Button btnCancel, btnCreate;
    private ImageButton btnBack;
    private DatabaseReference mDatabase;
    private List<String> groupList = new ArrayList<>();
    private GroupSpinnerAdapter groupAdapter;
    private Calendar selectedDateTime = Calendar.getInstance();
    private Priority selectedPriority;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        mDatabase = FirebaseDatabase.getInstance().getReference();

        initViews();
        loadUserGroups();
        setupListeners();

        if (getIntent().hasExtra("TASK_OBJECT")) {
            isEditMode = true;
            Task taskToEdit = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
            editTaskId = taskToEdit.getTaskId();

            setTitle("Chỉnh sửa công việc");
            btnCreate.setText("Lưu thay đổi");

            populateTaskData(taskToEdit);
        }
    }

    private void populateTaskData(Task task) {
        etTaskTitle.setText(task.getTitle());
        etTaskDescription.setText(task.getDescription());

        if (task.getDeadline() != null && !task.getDeadline().isEmpty()) {
            tvDeadline.setText(task.getDeadline());
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US);
            try {
                selectedDateTime.setTime(sdf.parse(task.getDeadline()));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        String priority = task.getPriority();
        if (priority != null) {
            switch (priority) {
                case "Cao":
                    chipGroupPriority.check(R.id.chipHigh);
                    break;
                case "Trung bình":
                    chipGroupPriority.check(R.id.chipMedium);
                    break;
                default:
                    chipGroupPriority.check(R.id.chipLow);
                    break;
            }
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
        btnCancel = findViewById(R.id.btnCancel);
        btnCreate = findViewById(R.id.btnCreate);
        btnBack = findViewById(R.id.btnBack);

        updateDeadlineText();

        groupAdapter = new GroupSpinnerAdapter(this, groupList, this);
        spinnerGroup.setAdapter(groupAdapter);
    }

    private void loadUserGroups() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) return;

        final Set<String> uniqueGroups = new HashSet<>();
        DatabaseReference userRef = mDatabase.child("users").child(userId);

        userRef.child("groups").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot groupsSnapshot) {
                for (DataSnapshot groupSnapshot : groupsSnapshot.getChildren()) {
                    String groupName = groupSnapshot.getValue(String.class);
                    if (groupName != null) uniqueGroups.add(groupName);
                }
                groupList.clear();
                groupList.addAll(uniqueGroups);
                groupAdapter.notifyDataSetChanged();

                if (isEditMode) {
                    Task taskToEdit = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
                    if (taskToEdit != null && taskToEdit.getGroup() != null) {
                        int pos = groupAdapter.getPosition(taskToEdit.getGroup());
                        if (pos >= 0) spinnerGroup.setSelection(pos);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AddTaskActivity.this, "Lỗi tải danh sách nhóm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnCancel.setOnClickListener(v -> finish());
        tvAddGroup.setOnClickListener(v -> showCreateGroupDialog());
        btnCreate.setOnClickListener(v -> saveTask());

        if (findViewById(R.id.layoutDeadline) != null) {
            findViewById(R.id.layoutDeadline).setOnClickListener(v -> showDateTimePicker());
        }

        chipGroupPriority.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipLow) selectedPriority = Priority.LOW;
            else if (checkedId == R.id.chipMedium) selectedPriority = Priority.MEDIUM;
            else if (checkedId == R.id.chipHigh) selectedPriority = Priority.HIGH;
        });

        chipGroupReminder.check(R.id.chipNone);
        chipGroupPriority.check(R.id.chipLow);

        Chip chipCustom = findViewById(R.id.chipCustom);
        if (chipCustom != null) {
            chipCustom.setOnClickListener(v -> showCustomReminderDialog());
        }


    }

    private void saveTask() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            Toast.makeText(this, "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTaskTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tiêu đề", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerGroup.getSelectedItem() == null) {
            Toast.makeText(this, "Vui lòng chọn hoặc tạo nhóm", Toast.LENGTH_SHORT).show();
            return;
        }

        String description = etTaskDescription.getText().toString().trim();
        String group = spinnerGroup.getSelectedItem().toString();
        String deadline = tvDeadline.getText().toString();
        String priority = "Thấp";
        int checkedPriorityId = chipGroupPriority.getCheckedChipId();
        if (checkedPriorityId == R.id.chipHigh) priority = "Cao";
        else if (checkedPriorityId == R.id.chipMedium) priority = "Trung bình";

        String taskId = isEditMode ? editTaskId : mDatabase.child("users").child(userId).child("tasks").push().getKey();
        if (taskId == null) return;

        Map<String, Object> taskValues = new HashMap<>();
        taskValues.put("title", title);
        taskValues.put("description", description);
        taskValues.put("Group", group);
        taskValues.put("deadline", deadline);
        taskValues.put("priority", priority);

        DatabaseReference taskRef = mDatabase.child("users").child(userId).child("tasks").child(taskId);

        if (isEditMode) {
            Task originalTask = (Task) getIntent().getSerializableExtra("TASK_OBJECT");
            if (originalTask != null) {
                taskValues.put("completed", originalTask.isCompleted());
            }
            taskRef.updateChildren(taskValues).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
        } else {
            taskValues.put("completed", false);
            taskValues.put("createAt", getCurrentTime());
            taskRef.setValue(taskValues).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Tạo thành công", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    @Override
    public void onDeleteGroup(String groupName, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Xác nhận xóa nhóm")
                .setMessage("Bạn có chắc chắn muốn xóa nhóm \"" + groupName + "\"?\n\nCông việc thuộc nhóm này sẽ không bị xóa.")
                .setPositiveButton("Xóa", (dialog, which) -> performDeleteGroup(groupName))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDeleteGroup(String groupName) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) {
            Toast.makeText(this, "Không thể xác định người dùng.", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference groupsRef = mDatabase.child("users").child(userId).child("groups");
        Query groupQuery = groupsRef.orderByValue().equalTo(groupName);

        groupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(AddTaskActivity.this, "Không tìm thấy nhóm để xóa.", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().removeValue().addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddTaskActivity.this, "Đã xóa nhóm: " + groupName, Toast.LENGTH_SHORT).show();
                        groupList.remove(groupName);
                        groupAdapter.notifyDataSetChanged();
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(AddTaskActivity.this, "Lỗi: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onEditGroup(String oldGroupName, int position) {
        final EditText input = new EditText(this);
        input.setText(oldGroupName);

        new AlertDialog.Builder(this)
                .setTitle("Chỉnh sửa tên nhóm")
                .setView(input)
                .setPositiveButton("Lưu", (dialog, which) -> {
                    String newGroupName = input.getText().toString().trim();
                    if (!newGroupName.isEmpty() && !newGroupName.equals(oldGroupName)) {
                        performEditGroup(oldGroupName, newGroupName, position);
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performEditGroup(String oldGroupName, String newGroupName, int position) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", null);
        if (userId == null) return;

        DatabaseReference groupsRef = mDatabase.child("users").child(userId).child("groups");
        Query groupQuery = groupsRef.orderByValue().equalTo(oldGroupName);

        groupQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    Toast.makeText(AddTaskActivity.this, "Không tìm thấy nhóm để sửa.", Toast.LENGTH_SHORT).show();
                    return;
                }
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    snapshot.getRef().setValue(newGroupName).addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddTaskActivity.this, "Đã cập nhật nhóm", Toast.LENGTH_SHORT).show();
                        groupList.set(position, newGroupName);
                        groupAdapter.notifyDataSetChanged();
                        spinnerGroup.setSelection(position);
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
    }

    private void showDateTimePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            selectedDateTime.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                selectedDateTime.set(Calendar.MINUTE, minute);
                updateDeadlineText();
            }, selectedDateTime.get(Calendar.HOUR_OF_DAY), selectedDateTime.get(Calendar.MINUTE), false).show();
        }, selectedDateTime.get(Calendar.YEAR), selectedDateTime.get(Calendar.MONTH), selectedDateTime.get(Calendar.DATE)).show();
    }

    private void updateDeadlineText() {
        String myFormat = "HH:mm dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        tvDeadline.setText(sdf.format(selectedDateTime.getTime()));
    }

    private String getCurrentTime() {
        return new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.US).format(Calendar.getInstance().getTime());
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

            if (userId == null) {
                dialog.dismiss();
                return;
            }

            DatabaseReference groupsRef = mDatabase.child("users").child(userId).child("groups");

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
                            });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        });

        dialog.show();
    }

    private void showCustomReminderDialog() {
        CustomReminderDialog dialog = new CustomReminderDialog(this, (days, hours, minutes) -> {
            Chip chipCustom = findViewById(R.id.chipCustom);
            if (chipCustom != null) {
                String customText = String.format("%d ngày %d giờ %d phút", days, hours, minutes);
                chipCustom.setText("Tùy chỉnh: " + customText);
                chipGroupReminder.check(R.id.chipCustom);
            }
        });
        dialog.show();
    }
}
