package com.example.projecttodo;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.Model.Reminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    // ===== Interface click =====
    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
    }

    private final List<Reminder> reminderList;
    private final OnReminderClickListener listener;

    // ===== Constructor =====
    public ReminderAdapter(List<Reminder> reminderList,
                           OnReminderClickListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
    }

    // ===== Tạo ViewHolder =====
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder, parent, false);
        return new ViewHolder(view);
    }

    // ===== Bind dữ liệu =====
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder r = reminderList.get(position);

        boolean isBirthday = "birthday".equals(r.type);

        // ===== ICON =====
        holder.tvIcon.setText(isBirthday ? "🎂" : "⏰");

        // ===== FORMAT NGÀY THÁNG (dd/MM/yy) =====
        String dateStr = "";
        if (r.date != null && !r.date.isEmpty()) {
            // Giả sử r.date có định dạng "yyyy-MM-dd" (thường dùng trong DB hoặc Calendar)
            // Nếu định dạng khác, bạn điều chỉnh ở đây
            try {
                String[] parts = r.date.split("-"); // "2025-12-23" → ["2025", "12", "23"]
                if (parts.length == 3) {
                    String day = parts[2];
                    String month = parts[1];
                    String year = parts[0].substring(2); // lấy 2 số cuối của năm: 25
                    dateStr = day + "/" + month + "/" + year + " - ";
                }
            } catch (Exception e) {
                // Nếu lỗi parse, bỏ qua ngày
                dateStr = "";
            }
        }

        // ===== TEXT =====
        String text;
        if (isBirthday) {
            String name = (r.name == null) ? "" : r.name;
            text = dateStr + r.time + " - Sinh nhật " + name;
        } else {
            String title = (r.title == null || r.title.isEmpty())
                    ? "Nhắc hẹn"
                    : r.title;
            text = dateStr + r.time + " - " + title;
        }

        holder.tvText.setText(text);

        // ===== STYLE =====
        holder.tvText.setTextSize(16);
        holder.tvText.setTypeface(null, Typeface.BOLD);

        // ===== CLICK =====
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReminderClick(r);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reminderList == null ? 0 : reminderList.size();
    }

    // ===== ViewHolder =====
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon;
        TextView tvText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            tvText = itemView.findViewById(R.id.tvText);
        }
    }
}
