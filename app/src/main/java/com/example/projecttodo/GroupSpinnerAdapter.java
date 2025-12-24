package com.example.projecttodo;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

public class GroupSpinnerAdapter extends ArrayAdapter<String> {

    private final LayoutInflater inflater;
    private final Context context;
    private final GroupActionListener listener;

    // Interface để gửi sự kiện click ra bên ngoài
    public interface GroupActionListener {
        void onDeleteGroup(String groupName, int position);
    }

    public GroupSpinnerAdapter(@NonNull Context context, @NonNull List<String> groups, GroupActionListener listener) {
        super(context, 0, groups);
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Giao diện cho mục được chọn (khi spinner đóng)
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(android.R.layout.simple_spinner_item, parent, false);
        }

        String group = getItem(position);
        TextView textView = view.findViewById(android.R.id.text1);
        textView.setText(group);
        return view;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Giao diện cho các mục trong danh sách thả xuống
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.spinner_item, parent, false);
        }

        String group = getItem(position);

        TextView groupName = view.findViewById(android.R.id.text1);
        ImageView deleteIcon = view.findViewById(R.id.icon_delete_group);

        groupName.setText(group);

        // Tô màu cho icon
        int iconColor = ContextCompat.getColor(context, R.color.button_purple); // Bạn có thể đổi màu ở đây
        deleteIcon.setColorFilter(iconColor, PorterDuff.Mode.SRC_IN);

        // Xử lý sự kiện click
        deleteIcon.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteGroup(group, position);
            }
        });

        return view;
    }
}
