package com.example.projecttodo;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class SelectGroupDialog extends Dialog {

    private GridLayout gridGroups;
    private Button btnAddGroup;

    private OnGroupSelectedListener listener;
    private String selectedGroup = "";

    // Array of groups
    private static final GroupItem[] GROUPS = {
            new GroupItem("Grocery", "#CCDD22"),
            new GroupItem("Work", "#FF9988"),
            new GroupItem("Sport", "#66EEDD"),
            new GroupItem("Design", "#88DDCC"),
            new GroupItem("University", "#7788DD"),
            new GroupItem("Social", "#EE77CC"),
            new GroupItem("Music", "#DD77BB"),
            new GroupItem("Health", "#77DD77"),
            new GroupItem("Movie", "#77BBDD"),
            new GroupItem("Home", "#FFCC88"),
            new GroupItem("Tạo mới", "#66DDAA", true)
    };

    public interface OnGroupSelectedListener {
        void onGroupSelected(String groupName);
        void onCreateNewGroup();
    }

    public SelectGroupDialog(@NonNull Context context, OnGroupSelectedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_select_group);

        // Set dialog background transparent
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        populateGroups();
        setupListeners();
    }

    private void initViews() {
        gridGroups = findViewById(R.id.gridGroups);
        btnAddGroup = findViewById(R.id.btnAddGroup);
    }

    private void populateGroups() {
        LayoutInflater inflater = LayoutInflater.from(getContext());

        for (GroupItem group : GROUPS) {
            View itemView = inflater.inflate(R.layout.item_group_grid, gridGroups, false);

            View viewColor = itemView.findViewById(R.id.viewGroupColor);
            ImageView ivIcon = itemView.findViewById(R.id.ivGroupIcon);
            TextView tvName = itemView.findViewById(R.id.tvGroupName);

            // Set color
            viewColor.setBackgroundColor(Color.parseColor(group.color));

            // Set name
            tvName.setText(group.name);

            // Show icon for "Create New"
            if (group.isCreateNew) {
                ivIcon.setVisibility(View.VISIBLE);
                ivIcon.setImageResource(R.drawable.ic_add);
            }

            // Set click listener
            itemView.setOnClickListener(v -> {
                if (group.isCreateNew) {
                    dismiss();
                    if (listener != null) {
                        listener.onCreateNewGroup();
                    }
                } else {
                    selectGroup(group.name);
                }
            });

            gridGroups.addView(itemView);
        }
    }

    private void setupListeners() {
        btnAddGroup.setOnClickListener(v -> {
            if (selectedGroup.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn nhóm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onGroupSelected(selectedGroup);
            }
            dismiss();
        });
    }

    private void selectGroup(String groupName) {
        selectedGroup = groupName;
        Toast.makeText(getContext(), "Đã chọn: " + groupName, Toast.LENGTH_SHORT).show();
        // TODO: Update UI to show selected group with visual feedback
    }

    // Helper class to store group data
    static class GroupItem {
        String name;
        String color;
        boolean isCreateNew;

        GroupItem(String name, String color) {
            this(name, color, false);
        }

        GroupItem(String name, String color, boolean isCreateNew) {
            this.name = name;
            this.color = color;
            this.isCreateNew = isCreateNew;
        }
    }
}