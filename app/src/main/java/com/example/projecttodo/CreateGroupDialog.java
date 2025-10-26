package com.example.projecttodo;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class CreateGroupDialog extends Dialog {

    private EditText etGroupName;
    private LinearLayout layoutColors;
    private Button btnCancelCreate, btnCreateGroup;

    private OnGroupCreatedListener listener;
    private String selectedColor = "#CCDD22"; // Default yellow

    // Array of colors
    private static final String[] COLORS = {
            "#CCDD22", // Yellow
            "#66DD66", // Green
            "#22DDBB", // Teal
            "#4499DD", // Blue
            "#44BBEE", // Light Blue
            "#EE9944", // Orange
            "#BB66DD", // Purple
            "#EE5599"  // Pink
    };

    public interface OnGroupCreatedListener {
        void onGroupCreated(String groupName, String color);
    }

    public CreateGroupDialog(@NonNull Context context, OnGroupCreatedListener listener) {
        super(context);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_create_group);

        // Set dialog to fullscreen
        if (getWindow() != null) {
            getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT);
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        initViews();
        populateColors();
        setupListeners();
    }

    private void initViews() {
        etGroupName = findViewById(R.id.etGroupName);
        layoutColors = findViewById(R.id.layoutColors);
        btnCancelCreate = findViewById(R.id.btnCancelCreate);
        btnCreateGroup = findViewById(R.id.btnCreateGroup);
    }

    private void populateColors() {
        for (String color : COLORS) {
            View colorView = new View(getContext());

            // Set layout params
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(40), // width
                    dpToPx(40)  // height
            );
            params.setMarginEnd(dpToPx(12));
            colorView.setLayoutParams(params);

            // Set color
            colorView.setBackgroundResource(R.drawable.bg_color_circle);
            colorView.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor(color)));

            // Set click listener
            colorView.setOnClickListener(v -> selectColor(color));

            // Make clickable
            colorView.setClickable(true);
            colorView.setFocusable(true);

            layoutColors.addView(colorView);
        }
    }

    private void setupListeners() {
        btnCancelCreate.setOnClickListener(v -> dismiss());

        btnCreateGroup.setOnClickListener(v -> {
            String groupName = etGroupName.getText().toString().trim();

            if (groupName.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên nhóm", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onGroupCreated(groupName, selectedColor);
            }

            Toast.makeText(getContext(), "Đã tạo nhóm: " + groupName, Toast.LENGTH_SHORT).show();
            dismiss();
        });
    }

    private void selectColor(String color) {
        selectedColor = color;
        Toast.makeText(getContext(), "Đã chọn màu", Toast.LENGTH_SHORT).show();
        // TODO: Update UI to show selected color with visual feedback (border, checkmark, etc.)
    }

    private int dpToPx(int dp) {
        float density = getContext().getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}