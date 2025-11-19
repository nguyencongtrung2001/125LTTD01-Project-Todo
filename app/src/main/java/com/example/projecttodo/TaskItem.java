package com.example.projecttodo;

public class TaskItem {

    // Các trường phải khớp tên chính xác với Firebase
    private String taskId;
    private String title;
    private String deadline;
    private boolean completed;
    private String description;

    // Yêu cầu bắt buộc: Constructor mặc định KHÔNG đối số cho Firebase
    public TaskItem() {
    }

    // --- Getters và Setters ---
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public String getDeadline() { return deadline; }
    public boolean isCompleted() { return completed; }
    public String getDescription() { return description; }

    // Bạn có thể thêm các getters/setters khác (group, createdAt, priority)
}