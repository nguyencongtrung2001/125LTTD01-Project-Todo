package com.example.projecttodo;

import java.io.Serializable;

public class Task implements Serializable {
    private String taskId, title, deadline, group, description;
    private boolean completed;

    public Task() {}

    public Task(String taskId, String title, String deadline, String group, boolean completed, String description) {
        this.taskId = taskId;
        this.title = title;
        this.deadline = deadline;
        this.group = group;
        this.completed = completed;
        this.description = description;
    }

    // Getters và Setters giữ nguyên như cũ
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
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}