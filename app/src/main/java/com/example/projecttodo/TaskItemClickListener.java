package com.example.projecttodo;

public interface TaskItemClickListener {
    void onTaskClick(Task task); // Sự kiện click để xem chi tiết
    void onSelectionModeChange(boolean isSelecting); // Sự kiện khi chọn nhiều
}