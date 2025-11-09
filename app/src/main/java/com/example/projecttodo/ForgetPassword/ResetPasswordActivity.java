package com.example.projecttodo.ForgetPassword;

import android.content.Intent;
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

import android.os.Bundle;

import com.example.projecttodo.LoginActivity;
import com.example.projecttodo.R;
import com.google.firebase.database.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText, confirmPasswordEditText;
    private Button resetButton;
    private ImageButton btnBack;
    private ImageView toggleNewPassword, toggleConfirmPassword;
    private ProgressBar progressBar;

    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private String userEmail;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        userEmail = getIntent().getStringExtra("email");
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

        toggleNewPassword.setOnClickListener(v -> toggleVisibility(newPasswordEditText, toggleNewPassword));
        toggleConfirmPassword.setOnClickListener(v -> toggleVisibility(confirmPasswordEditText, toggleConfirmPassword));

        resetButton.setOnClickListener(v -> handleResetPassword());
    }

    // Ẩn/hiện mật khẩu
    private void toggleVisibility(EditText field, ImageView toggle) {
        boolean visible = (field.getInputType() & InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
                == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD;
        if (visible) {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            toggle.setImageResource(R.drawable.ic_eye_off);
        } else {
            field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            toggle.setImageResource(R.drawable.ic_eye_on);
        }
        field.setSelection(field.getText().length());
    }

    private void handleResetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        if (!validateInput(newPassword, confirmPassword)) return;

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
        if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // Cập nhật mật khẩu mới trong Firebase
    private void updatePassword(String newPassword) {
        Query query = databaseReference.orderByChild("email").equalTo(userEmail);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    setLoading(false);
                    Toast.makeText(ResetPasswordActivity.this, "Không tìm thấy người dùng", Toast.LENGTH_SHORT).show();
                    return;
                }

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    String hashedPassword = hashPassword(newPassword);

                    // Cập nhật mật khẩu
                    databaseReference.child(userId).child("password").setValue(hashedPassword)
                            .addOnSuccessListener(aVoid -> {
                                setLoading(false);
                                Toast.makeText(ResetPasswordActivity.this, "Đặt lại mật khẩu thành công!", Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("registered_email", userEmail);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                Toast.makeText(ResetPasswordActivity.this, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            });
                    break;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(ResetPasswordActivity.this, "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Mã hóa SHA-256 giống phần đăng ký
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                String h = Integer.toHexString(0xff & b);
                if (h.length() == 1) hex.append('0');
                hex.append(h);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null)
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        resetButton.setEnabled(!isLoading);
        newPasswordEditText.setEnabled(!isLoading);
        confirmPasswordEditText.setEnabled(!isLoading);
    }
}
