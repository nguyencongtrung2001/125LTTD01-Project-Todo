// File: com.example.projecttodo.TaskItemClickListener.java

package com.example.projecttodo;

public interface TaskItemClickListener {
    // Sự kiện mở chi tiết
    void onTaskClick(Task task);

    // Sự kiện khi chế độ chọn (multi-select) thay đổi (để hiện/ẩn thanh công cụ xóa hàng loạt)
    void onSelectionModeChange(boolean isSelecting);
}