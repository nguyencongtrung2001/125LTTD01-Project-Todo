package com.example.projecttodo;

public class Task {

    private String taskId; // 🔹 thêm taskId
    private String title;
    private String deadline;
    private String group;
    private boolean completed;

    public Task() {}

    public Task(String taskId, String title, String deadline, String group, boolean completed) {
        this.taskId = taskId;
        this.title = title;
        this.deadline = deadline;
        this.group = group;
        this.completed = completed;
    }

    // getter + setter
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
