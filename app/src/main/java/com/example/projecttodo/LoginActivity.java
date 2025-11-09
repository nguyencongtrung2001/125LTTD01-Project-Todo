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

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupTextView, tvForgotPassword;
    private ProgressBar progressBar;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Áp dụng theme đã lưu trước đó (dark/light)
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

        // Kết nối Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();

        // Điền sẵn email nếu vừa đăng ký
        Intent intent = getIntent();
        if (intent.hasExtra("registered_email")) {
            emailEditText.setText(intent.getStringExtra("registered_email"));
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
        loginButton.setOnClickListener(v -> handleLogin());
        signupTextView.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void handleLogin() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (!validateInput(email, password)) return;

        setLoading(true);
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

                boolean loginSuccess = false;
                String userId = "";
                String fullName = "";

                // Kiểm tra mật khẩu
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
                    saveLoginInfo(userId, fullName, email);
                    Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();

                    // Sau khi login thành công → lấy cài đặt người dùng
                    applyUserSettings(userId);

                    // Lưu lại tab mặc định là "home"
                    SharedPreferences navPrefs = getSharedPreferences("nav_state", MODE_PRIVATE);
                    navPrefs.edit().putString("last_tab", "home").apply();

                    // Mở HomeActivity
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

    // Áp dụng thiết lập người dùng từ Firebase (darkMode, language, notification)
    private void applyUserSettings(String userId) {
        DatabaseReference settingsRef = databaseReference.child(userId).child("settings");

        settingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();

                // Dark mode
                boolean darkMode = snapshot.child("darkMode").getValue(Boolean.class) != null &&
                        snapshot.child("darkMode").getValue(Boolean.class);
                editor.putBoolean("dark_mode", darkMode);

                // Ngôn ngữ (comment lại, sẽ xử lý sau)
                // String language = snapshot.child("language").getValue(String.class);
                // if (language != null) editor.putString("language", language);

                // Thông báo (comment lại, sẽ xử lý sau)
                // boolean notification = snapshot.child("notification").getValue(Boolean.class) != null &&
                //         snapshot.child("notification").getValue(Boolean.class);
                // editor.putBoolean("notification", notification);

                editor.apply();

                // Áp dụng lại darkMode ngay
                AppCompatDelegate.setDefaultNightMode(
                        darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Không tải được cài đặt người dùng", Toast.LENGTH_SHORT).show();
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
        if (progressBar != null)
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
    }
}
