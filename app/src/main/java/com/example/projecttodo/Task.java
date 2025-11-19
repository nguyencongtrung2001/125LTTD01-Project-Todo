package com.example.projecttodo; // Dùng package 'model' như trong image_fc1c65.png

import com.google.firebase.database.PropertyName;

public class Task {
    // Tên trường trong Java (thường)
    private String title;
    private String description;
    private String deadline;

    // Ánh xạ key "Group" (từ Firebase) vào field 'group' (trong Java)
    @PropertyName("Group")
    private String group;

    private boolean completed;
    private String createAt;
    private String priority;

    // BẮT BUỘC: Constructor mặc định KHÔNG THAM SỐ cho Firebase
    public Task() {
    }

    // Constructor đầy đủ (Tùy chọn)
    public Task(String title, String description, String deadline, String group, boolean completed, String createAt, String priority) {
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.group = group;
        this.completed = completed;
        this.createAt = createAt;
        this.priority = priority;
    }

    // Getters và Setters (Viết hoa chữ cái đầu sau 'get'/'set')
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Đảm bảo tên phương thức chính xác là getDeadline() (D hoa)
    public String getdeadline() { return deadline; }
    public void setDeadline(String deadline) { this.deadline = deadline; }

    // Getter/Setter cho group
    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    // Phương thức tiêu chuẩn cho boolean
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getCreateAt() { return createAt; }
    public void setCreateAt(String createAt) { this.createAt = createAt; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }


}