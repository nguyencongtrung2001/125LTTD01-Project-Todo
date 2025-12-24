package com.example.projecttodo;

import java.io.Serializable;

public class Task implements Serializable {

    private String taskId;
    private String title;
    private String deadline;
    private String group;
    private boolean completed;
    private String description;

    // 👉 PRIORITY: "Cao" | "Trung bình" | "Thấp"
    private String priority;

    // Firebase bắt buộc constructor rỗng
    public Task() {
        this.priority = "Trung bình"; // default an toàn
    }

    public Task(String taskId,
                String title,
                String deadline,
                String group,
                boolean completed,
                String description,
                String priority) {

        this.taskId = taskId;
        this.title = title;
        this.deadline = deadline;
        this.group = group;
        this.completed = completed;
        this.description = description;

        // đảm bảo priority hợp lệ
        setPriority(priority);
    }

    // ===== Getter & Setter cũ =====

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

    // ===== PRIORITY (YÊU CẦU CỦA BẠN) =====

    /**
     * @return "Cao", "Trung bình", hoặc "Thấp"
     */
    public String getPriority() {
        if (priority == null || priority.isEmpty()) {
            return "Trung bình"; // fallback an toàn
        }
        return priority;
    }

    /**
     * Chỉ chấp nhận 3 giá trị hợp lệ
     */
    public void setPriority(String priority) {
        if ("Cao".equals(priority) ||
                "Trung bình".equals(priority) ||
                "Thấp".equals(priority)) {

            this.priority = priority;
        } else {
            this.priority = "Trung bình"; // default nếu sai
        }
    }
}
