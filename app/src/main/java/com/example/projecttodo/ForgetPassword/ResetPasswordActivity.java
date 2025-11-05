package com.example.projecttodo.ForgetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecttodo.LoginActivity;
import com.example.projecttodo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private EditText confirmPasswordEditText;
    private Button resetButton;
    private ImageButton btnBack;
    private ImageView toggleNewPassword;
    private ImageView toggleConfirmPassword;
    private ProgressBar progressBar;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private String userEmail;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Nhận email từ intent
        userEmail = getIntent().getStringExtra("email");

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();
    }

    private void initViews() {
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        resetButton = findViewById(R.id.resetButton);
        btnBack = findViewById(R.id.btnBack);
        toggleNewPassword = findViewById(R.id.toggleNewPassword);
        toggleConfirmPassword = findViewById(R.id.toggleConfirmPassword);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Toggle hiển thị mật khẩu mới
        toggleNewPassword.setOnClickListener(v -> {
            if (isNewPasswordVisible) {
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleNewPassword.setImageResource(R.drawable.ic_eye_off);
                isNewPasswordVisible = false;
            } else {
                newPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleNewPassword.setImageResource(R.drawable.ic_eye_on);
                isNewPasswordVisible = true;
            }
            newPasswordEditText.setSelection(newPasswordEditText.getText().length());
        });

        // Toggle hiển thị xác nhận mật khẩu
        toggleConfirmPassword.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_off);
                isConfirmPasswordVisible = false;
            } else {
                confirmPasswordEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPassword.setImageResource(R.drawable.ic_eye_on);
                isConfirmPasswordVisible = true;
            }
            confirmPasswordEditText.setSelection(confirmPasswordEditText.getText().length());
        });

        resetButton.setOnClickListener(v -> handleResetPassword());
    }

    private void handleResetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInput(newPassword, confirmPassword)) {
            return;
        }

        setLoading(true);
        updatePassword(newPassword);
    }

    private boolean validateInput(String newPassword, String confirmPassword) {
        if (newPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng xác nhận mật khẩu", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updatePassword(String newPassword) {
        Query query = databaseReference.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String userId = userSnapshot.getKey();
                        String hashedPassword = hashPassword(newPassword);

                        // Cập nhật mật khẩu mới
                        databaseReference.child(userId).child("password").setValue(hashedPassword)
                                .addOnSuccessListener(aVoid -> {
                                    setLoading(false);
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                                    // Chuyển về màn hình đăng nhập
                                    Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.putExtra("registered_email", userEmail);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    setLoading(false);
                                    Toast.makeText(ResetPasswordActivity.this,
                                            "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                        break;
                    }
                } else {
                    setLoading(false);
                    Toast.makeText(ResetPasswordActivity.this,
                            "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(ResetPasswordActivity.this,
                        "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
        resetButton.setEnabled(!isLoading);
        newPasswordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
    }
}