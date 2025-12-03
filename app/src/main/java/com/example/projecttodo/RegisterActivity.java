package com.example.projecttodo;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecttodo.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button registerButton;
    private TextView loginTextView;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;
    private ProgressBar progressBar;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();
    }

    // Ánh xạ các view trong layout
    private void initViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        loginTextView = findViewById(R.id.loginTextView);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        progressBar = findViewById(R.id.progressBar);
    }

    // Thiết lập các sự kiện click
    private void setupListeners() {
        // Ẩn/hiện mật khẩu
        togglePasswordVisibility.setOnClickListener(v -> {
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
        });

        // Ẩn/hiện xác nhận mật khẩu
        toggleConfirmPasswordVisibility.setOnClickListener(v -> {
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
        });

        // Xử lý nút đăng ký
        registerButton.setOnClickListener(v -> handleRegister());

        // Chuyển sang màn hình đăng nhập
        loginTextView.setOnClickListener(v -> finish());
    }

    // Xử lý đăng ký
    private void handleRegister() {
        String fullName = fullNameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInput(fullName, email, password, confirmPassword)) {
            return;
        }

        setLoading(true);
        checkEmailExists(email, fullName, password);
    }

    // Kiểm tra dữ liệu nhập vào
    private boolean validateInput(String fullName, String email, String password, String confirmPassword) {
        if (fullName.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_fullname, Toast.LENGTH_SHORT).show();
            return false;
        }

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

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    // Kiểm tra email đã tồn tại trong Firebase
    private void checkEmailExists(String email, String fullName, String password) {
        Query query = databaseReference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Email đã được sử dụng", Toast.LENGTH_SHORT).show();
                } else {
                    registerUser(fullName, email, password);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Đăng ký tài khoản mới và thêm cấu trúc mặc định
    private void registerUser(String fullName, String email, String password) {
        String userId = databaseReference.push().getKey();

        if (userId == null) {
            setLoading(false);
            Toast.makeText(this, "Lỗi tạo tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = hashPassword(password);
        User user = new User(userId, fullName, email, hashedPassword, System.currentTimeMillis());

        // Ghi dữ liệu người dùng
        databaseReference.child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    DatabaseReference userRef = databaseReference.child(userId);

                    // Thêm thông tin người dùng
                    userRef.child("info").child("name").setValue(fullName);
                    userRef.child("info").child("email").setValue(email);
                    userRef.child("info").child("createdAt").setValue(System.currentTimeMillis());

                    // Cài đặt mặc định
                    userRef.child("settings").child("darkMode").setValue(false);
                    userRef.child("settings").child("language").setValue("vi");
                    userRef.child("settings").child("notification").setValue(true);

                    // Nhóm mặc định
                    userRef.child("groups").child("group_001").setValue("Công việc cá nhân");
                    userRef.child("groups").child("group_002").setValue("Học tập");

                    // Thống kê tổng (all)
                    userRef.child("statistics").child("all").child("total").setValue(0);
                    userRef.child("statistics").child("all").child("completed").setValue(0);
                    userRef.child("statistics").child("all").child("incomplete").setValue(0);
                    userRef.child("statistics").child("all").child("overdue").setValue(0);

                    // Thống kê 7 ngày (last7Days)
                    userRef.child("statistics").child("last7Days").child("total").setValue(0);
                    userRef.child("statistics").child("last7Days").child("completed").setValue(0);
                    userRef.child("statistics").child("last7Days").child("incomplete").setValue(0);
                    userRef.child("statistics").child("last7Days").child("overdue").setValue(0);

                    // Thống kê 30 ngày (last30Days)
                    userRef.child("statistics").child("last30Days").child("total").setValue(0);
                    userRef.child("statistics").child("last30Days").child("completed").setValue(0);
                    userRef.child("statistics").child("last30Days").child("incomplete").setValue(0);
                    userRef.child("statistics").child("last30Days").child("overdue").setValue(0);

                    // Tạo node tasks rỗng (giữ nhánh tồn tại nhưng chưa có dữ liệu)
                    userRef.child("tasks").setValue("");

                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();

                    // Chuyển về màn hình đăng nhập
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra("registered_email", email);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(RegisterActivity.this, "Đăng ký thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Mã hóa mật khẩu bằng SHA-256
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

    // Ẩn/hiện ProgressBar khi đang xử lý
    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        registerButton.setEnabled(!isLoading);
        fullNameEditText.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
        passwordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
    }
}
