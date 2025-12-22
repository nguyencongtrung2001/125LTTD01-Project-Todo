package com.example.projecttodo.Model;

public class Reminder {
    public String id;

    public String date;        // yyyy-MM-dd
    public String time;        // HH:mm
    public long timestamp;

    public String message;     // nội dung hiển thị nhanh
    public boolean done;

    // ✅ THÊM MỚI
    public String type;        // "reminder" | "birthday"
    public String title;       // tiêu đề nhắc hẹn
    public String name;        // tên người sinh nhật
    public String detail;      // nội dung chi tiết (lời chúc)

    // Firebase cần constructor rỗng
    public Reminder() { }

    // Constructor đầy đủ (không bắt buộc dùng)
    public Reminder(String id, String date, String time, long timestamp,
                    String message, boolean done,
                    String type, String title, String name, String detail) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.message = message;
        this.done = done;
        this.type = type;
        this.title = title;
        this.name = name;
        this.detail = detail;
    }
}
