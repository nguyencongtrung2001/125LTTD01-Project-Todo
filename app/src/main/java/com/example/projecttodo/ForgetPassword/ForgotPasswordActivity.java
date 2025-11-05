package com.example.projecttodo.ForgetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecttodo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Button sendCodeButton;
    private ImageButton btnBack;
    private ProgressBar progressBar;
    private TextView tvDescription;

    private DatabaseReference databaseReference;
    private String verificationCode;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Khởi tạo Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        initViews();
        setupListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
        tvDescription = findViewById(R.id.tvDescription);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        sendCodeButton.setOnClickListener(v -> handleSendCode());
    }

    private void handleSendCode() {
        String email = emailEditText.getText().toString().trim();

        if (!validateEmail(email)) {
            return;
        }

        setLoading(true);
        checkEmailExists(email);
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void checkEmailExists(String email) {
        Query query = databaseReference.orderByChild("email").equalTo(email);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                setLoading(false);

                if (!snapshot.exists()) {
                    Toast.makeText(ForgotPasswordActivity.this,
                            "Email không tồn tại trong hệ thống", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Email tồn tại, tạo mã xác thực
                userEmail = email;
                generateAndSendCode();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setLoading(false);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Lỗi: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateAndSendCode() {
        // Tạo mã xác thực 6 chữ số
        Random random = new Random();
        verificationCode = String.format("%06d", random.nextInt(1000000));

        // Trong thực tế, bạn sẽ gửi code qua email
        // Ở đây ta chỉ hiển thị Toast để demo
        Toast.makeText(this,
                "Mã xác thực của bạn là: " + verificationCode +
                        "\n(Trong thực tế sẽ gửi qua email)",
                Toast.LENGTH_LONG).show();

        // Chuyển sang màn hình nhập mã
        Intent intent = new Intent(ForgotPasswordActivity.this, VerifyCodeActivity.class);
        intent.putExtra("email", userEmail);
        intent.putExtra("verification_code", verificationCode);
        startActivity(intent);
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        sendCodeButton.setEnabled(!isLoading);
        emailEditText.setEnabled(!isLoading);
    }
}