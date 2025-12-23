package com.example.projecttodo;


public interface TaskItemClickListener {
    void onTaskClick(Task task);
    void onEditTask(Task task);
    void onSelectionModeChange(boolean isSelecting);
}
