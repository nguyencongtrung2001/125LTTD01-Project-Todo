package com.example.projecttodo;

public class Task {

    private String title;
    private String deadline;
    private String group;
    private boolean completed;

    public Task() {
    }

    public Task(String title, String deadline, String group, boolean completed) {
        this.title = title;
        this.deadline = deadline;
        this.group = group;
        this.completed = completed;
    }

    public String getTitle() { return title; }
    public String getDeadline() { return deadline; }
    public String getGroup() { return group; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) { this.title = title; }
    public void setDeadline(String deadline) { this.deadline = deadline; }
    public void setGroup(String group) { this.group = group; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
