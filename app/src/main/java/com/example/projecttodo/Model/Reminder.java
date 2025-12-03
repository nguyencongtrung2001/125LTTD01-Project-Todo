package com.example.projecttodo.Model;

public class Reminder {
    public String id;
    public String date;        // "yyyy-MM-dd"
    public String time;        // "HH:mm"
    public long timestamp;     // millis UTC
    public String message;     // text hiển thị
    public boolean done;       // đã nhắc xong?

    public Reminder() { } // Firebase cần

    public Reminder(String id, String date, String time, long timestamp, String message, boolean done) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.message = message;
        this.done = done;
    }
}
