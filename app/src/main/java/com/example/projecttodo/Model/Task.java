package com.example.projecttodo.Model;

public class Task {
    private String id;
    private String title;
    private String description;
    private String Group;
    private String deadline;
    private String priority;
    private boolean completed;
    private String createAt;
    private String reminder;

    public Task() {
        // Default constructor required for calls to DataSnapshot.getValue(Task.class)
    }

    public Task(String id, String title, String description, String Group, String deadline, String priority, boolean completed, String createAt, String reminder) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.Group = Group;
        this.deadline = deadline;
        this.priority = priority;
        this.completed = completed;
        this.createAt = createAt;
        this.reminder = reminder;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getGroup() { return Group; }
    public void setGroup(String Group) { this.Group = Group; }

    public String getDeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }

    public String getReminder() { return reminder; }
    public void setReminder(String reminder) { this.reminder = reminder; }
}
