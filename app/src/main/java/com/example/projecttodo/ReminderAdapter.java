package com.example.projecttodo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projecttodo.Model.Reminder;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {

    public interface OnReminderClickListener {
        void onReminderClick(Reminder reminder);
    }

    private List<Reminder> reminderList;
    private OnReminderClickListener listener;

    // ===== Constructor =====
    public ReminderAdapter(List<Reminder> reminderList,
                           OnReminderClickListener listener) {
        this.reminderList = reminderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Reminder r = reminderList.get(position);
        if ("birthday".equals(r.type)) {

            String text = "🎂 " + r.time;
            if (r.name != null && !r.name.isEmpty()) {
                text += " - " + r.name;
            }
            holder.textView.setText(text);
        } else {

            String title = (r.title == null || r.title.isEmpty())
                    ? "Nhắc hẹn"
                    : r.title;
            holder.textView.setText("⏰ " + r.time + " - " + title);
        }
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

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
