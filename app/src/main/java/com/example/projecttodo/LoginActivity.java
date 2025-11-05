package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.projecttodo.ForgetPassword.ForgotPasswordActivity;
import com.example.projecttodo.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView signupTextView;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Thiết lập theme đúng trước khi tạo giao diện
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);

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

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();

        // Tự động điền email nếu vừa đăng ký
        Intent intent = getIntent();
        if (intent.hasExtra("registered_email")) {
            String email = intent.getStringExtra("registered_email");
            emailEditText.setText(email);
        }
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        loginButton = findViewById(R.id.loginButton);
        signupTextView = findViewById(R.id.signupTextView);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        // Xử lý đăng nhập
        loginButton.setOnClickListener(v -> handleLogin());

        // Xử lý chuyển sang màn hình Đăng ký
        signupTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Xử lý quên mật khẩu
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        // Validate input
        if (!validateInput(email, password)) {
            return;
        }

        // Hiển thị progress bar
        setLoading(true);

        // Kiểm tra đăng nhập
        checkLogin(email, password);
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_email, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_password, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkLogin(String email, String password) {
        Query query = databaseReference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);

                if (!snapshot.exists()) {
                    Toast.makeText(LoginActivity.this, "Email không tồn tại", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Lấy thông tin user
                boolean loginSuccess = false;
                String userId = "";
                String fullName = "";

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = userSnapshot.getValue(User.class);
                    if (user != null) {
                        String hashedPassword = hashPassword(password);
                        if (user.getPassword().equals(hashedPassword)) {
                            loginSuccess = true;
                            userId = user.getUserId();
                            fullName = user.getFullName();
                            break;
                        }
                    }
                }

                if (loginSuccess) {
                    // Lưu thông tin đăng nhập
                    saveLoginInfo(userId, fullName, email);

                    Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();

                    // Lưu lại tab cuối cùng là "home"
                    SharedPreferences navPrefs = getSharedPreferences("nav_state", MODE_PRIVATE);
                    navPrefs.edit().putString("last_tab", "home").apply();

                    // Chuyển sang màn hình Home
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Mật khẩu không đúng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginInfo(String userId, String fullName, String email) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_id", userId);
        editor.putString("full_name", fullName);
        editor.putString("email", email);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
    }
}