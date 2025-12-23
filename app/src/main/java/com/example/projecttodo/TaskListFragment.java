package com.example.projecttodo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskListFragment extends Fragment implements TaskItemClickListener {

    private static final String TAG = "TaskListFragment";

    private EditText searchEditText;
    private TextView chipAll, chipToday, chipUpcoming, chipOverdue, chipCompleted;
    private LinearLayout emptyStateLayout;
    private RecyclerView taskRecyclerView;
    private FloatingActionButton fabAddTask;

    private String userId;
    private String currentFilter = "all";
    private TaskAdapter taskAdapter;
    private List<Task> allTasks = new ArrayList<>();
    private DatabaseReference databaseReference;

    private final SimpleDateFormat DEADLINE_FORMAT = new SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        loadUserSession();
        if (userId.isEmpty()) {
            Toast.makeText(getContext(), "Lỗi phiên: Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
        } else {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("tasks");
        }

        initViews(view);

        taskRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        taskAdapter = new TaskAdapter(getContext(), new ArrayList<>(), userId, this);
        taskRecyclerView.setAdapter(taskAdapter);

        setupChipListeners();

        if (!userId.isEmpty()) {
            loadAllTasksFromFirebase();
        }

        selectChip(currentFilter);

        return view;
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        chipAll = view.findViewById(R.id.chipAll);
        chipToday = view.findViewById(R.id.chipToday);
        chipUpcoming = view.findViewById(R.id.chipUpcoming);
        chipOverdue = view.findViewById(R.id.chipOverdue);
        chipCompleted = view.findViewById(R.id.chipCompleted);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        taskRecyclerView = view.findViewById(R.id.taskRecyclerView);
        fabAddTask = view.findViewById(R.id.fabAddTask);
        searchEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Mỗi khi chữ thay đổi, cập nhật lại danh sách hiển thị
                updateTaskList();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }

    private void loadUserSession() {
        Context context = getContext();
        if (context != null) {
            SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
            userId = prefs.getString("user_id", "");
        }
    }

    private void setupChipListeners() {
        chipAll.setOnClickListener(v -> selectChip("all"));
        chipToday.setOnClickListener(v -> selectChip("today"));
        chipUpcoming.setOnClickListener(v -> selectChip("upcoming"));
        chipOverdue.setOnClickListener(v -> selectChip("overdue"));
        chipCompleted.setOnClickListener(v -> selectChip("completed"));

        fabAddTask.setOnClickListener(v -> {
            if (getActivity() != null) {
                startActivity(new Intent(getActivity(), AddTaskActivity.class));
            } else {
                Toast.makeText(getContext(), "Không thể mở màn hình thêm task", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectChip(String filter) {
        currentFilter = filter;

        TextView[] chips = {chipAll, chipToday, chipUpcoming, chipOverdue, chipCompleted};
        String[] filters = {"all", "today", "upcoming", "overdue", "completed"};

        for (int i = 0; i < chips.length; i++) {
            if (filters[i].equals(filter)) {
                chips[i].setBackgroundResource(R.drawable.chip_selected_background);
            } else {
                chips[i].setBackgroundResource(R.drawable.chip_background);
            }
        }

        updateTaskList();
    }

    private void loadAllTasksFromFirebase() {
        if (databaseReference == null) return;

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTasks.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot taskSnapshot : snapshot.getChildren()) {
                        Task task = taskSnapshot.getValue(Task.class);
                        if (task != null) {
                            task.setTaskId(taskSnapshot.getKey());
                            allTasks.add(task);
                        } else {
                            Log.e(TAG, "Task mapping failed for key: " + taskSnapshot.getKey());
                        }
                    }
                }
                updateTaskList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Lỗi tải dữ liệu Firebase: " + error.getMessage());
                Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTaskList() {
        List<Task> filteredTasks = new ArrayList<>();
        // Lấy từ khóa tìm kiếm và chuyển về chữ thường
        String query = searchEditText.getText().toString().trim().toLowerCase();

        Date now = new Date();
        String todayDateString = DATE_ONLY_FORMAT.format(now);

        for (Task task : allTasks) {
            // --- BƯỚC 1: LỌC THEO TỪ KHÓA (SEARCH) ---
            boolean matchesSearch = task.getTitle().toLowerCase().contains(query);
            if (!matchesSearch) continue; // Nếu không khớp tên thì bỏ qua luôn

            // --- BƯỚC 2: LỌC THEO CHIP (FILTER) ---
            boolean matchesFilter = false;
            Date taskDeadlineDate = null;
            try {
                taskDeadlineDate = DEADLINE_FORMAT.parse(task.getDeadline());
            } catch (ParseException e) {
                // Nếu lỗi ngày tháng, chỉ hiện ở tab "Tất cả" hoặc "Hoàn thành"
                if (currentFilter.equals("all")) matchesFilter = true;
            }

            String taskDateStr = (taskDeadlineDate != null) ? DATE_ONLY_FORMAT.format(taskDeadlineDate) : "";

            switch (currentFilter) {
                case "all":
                    matchesFilter = true;
                    break;
                case "today":
                    matchesFilter = taskDeadlineDate != null && taskDateStr.equals(todayDateString) && !task.isCompleted();
                    break;
                case "upcoming":
                    matchesFilter = !task.isCompleted() && taskDeadlineDate != null && taskDeadlineDate.after(now) && !taskDateStr.equals(todayDateString);
                    break;
                case "overdue":
                    matchesFilter = !task.isCompleted() && taskDeadlineDate != null && taskDeadlineDate.before(now) && !taskDateStr.equals(todayDateString);
                    break;
                case "completed":
                    matchesFilter = task.isCompleted();
                    break;
            }

            if (matchesFilter) {
                filteredTasks.add(task);
            }
        }

        // --- BƯỚC 3: SẮP XẾP ---
        // Task chưa xong lên đầu, xong rồi xuống dưới
        filteredTasks.sort((t1, t2) -> Boolean.compare(t1.isCompleted(), t2.isCompleted()));

        // Cập nhật lên giao diện
        taskAdapter.updateTasks(filteredTasks);

        // Hiển thị trạng thái trống nếu không tìm thấy gì
        if (filteredTasks.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
            taskRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            taskRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(getActivity(), TaskDetailActivity.class);
        intent.putExtra("TASK_OBJECT", task);
        startActivity(intent);
    }

    @Override
    public void onSelectionModeChange(boolean isSelecting) {
        // Không còn multi-select qua ivSelect → bỏ
    }
}
