package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signupTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Thiết lập theme đúng trước khi tạo giao diện
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

        // Nếu người dùng chưa chọn theme lần nào -> mặc định Light Mode
        if (!prefs.contains("dark_mode")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            boolean isDark = prefs.getBoolean("dark_mode", false);
            AppCompatDelegate.setDefaultNightMode(
                    isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ánh xạ view
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.signupTextView);

        // Xử lý đăng nhập
        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (email.isEmpty()) {
                Toast.makeText(LoginActivity.this, R.string.error_empty_email, Toast.LENGTH_SHORT).show();
                return;
            }

            if (password.isEmpty()) {
                Toast.makeText(LoginActivity.this, R.string.error_empty_password, Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();

            // Lưu lại tab cuối cùng là "home" để khi mở lại vẫn ở trang chính
            SharedPreferences navPrefs = getSharedPreferences("nav_state", MODE_PRIVATE);
            navPrefs.edit().putString("last_tab", "home").apply();

            // Chuyển sang màn hình Home
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Xử lý chuyển sang màn hình Đăng ký
        signupTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}
