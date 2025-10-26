package com.example.projecttodo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ImageView togglePasswordVisibility;
    private ImageView toggleConfirmPasswordVisibility;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo views
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);

        // Xử lý hiển thị/ẩn mật khẩu
        togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPasswordVisible) {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_eye_off);
                    isPasswordVisible = false;
                } else {
                    passwordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    togglePasswordVisibility.setImageResource(R.drawable.ic_eye_on);
                    isPasswordVisible = true;
                }
                passwordEditText.setSelection(passwordEditText.getText().length());
            }
        });

        // Xử lý hiển thị/ẩn xác nhận mật khẩu
        toggleConfirmPasswordVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isConfirmPasswordVisible) {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toggleConfirmPasswordVisibility.setImageResource(R.drawable.ic_eye_off);
                    isConfirmPasswordVisible = false;
                } else {
                    confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    toggleConfirmPasswordVisibility.setImageResource(R.drawable.ic_eye_on);
                    isConfirmPasswordVisible = true;
                }
                confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
            }
        });

        // Xử lý sự kiện click nút Đăng ký
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fullName = fullNameEditText.getText().toString().trim();
                String email = emailEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();
                String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                if (fullName.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, R.string.error_empty_fullname, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, R.string.error_empty_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, R.string.error_empty_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (confirmPassword.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, R.string.error_empty_confirm_password, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(RegisterActivity.this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
                    return;
                }

                // Xử lý đăng ký ở đây
                Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();

                // Chuyển về màn hình đăng nhập
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Xử lý chuyển sang màn hình Đăng nhập
        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}