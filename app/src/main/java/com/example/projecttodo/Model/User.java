package com.example.projecttodo.Model;
public class User {
    private String userId;
    private String fullName;
    private String email;
    private String password;
    private long createdAt;

    // Constructor rỗng (bắt buộc cho Firebase)
    public User() {
    }

    // Constructor đầy đủ
    public User(String userId, String fullName, String email, String password, long createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.password = password;
        this.createdAt = createdAt;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}