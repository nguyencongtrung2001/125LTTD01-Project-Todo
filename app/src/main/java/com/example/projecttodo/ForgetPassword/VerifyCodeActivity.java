package com.example.projecttodo.ForgetPassword;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.projecttodo.R;

public class VerifyCodeActivity extends AppCompatActivity {

    private EditText code1, code2, code3, code4, code5, code6;
    private Button verifyButton;
    private TextView tvResendCode, tvTimer;
    private ImageButton btnBack;
    private ProgressBar progressBar;

    private String userEmail;
    private String correctCode;
    private CountDownTimer countDownTimer;
    private boolean canResend = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_code);

        // Nhận dữ liệu từ ForgotPasswordActivity
        userEmail = getIntent().getStringExtra("email");
        correctCode = getIntent().getStringExtra("verification_code");

        initViews();
        setupListeners();
        startCountdown();
    }

    private void initViews() {
        code1 = findViewById(R.id.code1);
        code2 = findViewById(R.id.code2);
        code3 = findViewById(R.id.code3);
        code4 = findViewById(R.id.code4);
        code5 = findViewById(R.id.code5);
        code6 = findViewById(R.id.code6);
        verifyButton = findViewById(R.id.verifyButton);
        tvResendCode = findViewById(R.id.tvResendCode);
        tvTimer = findViewById(R.id.tvTimer);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Tự chuyển focus
        setupAutoFocus(code1, code2);
        setupAutoFocus(code2, code3);
        setupAutoFocus(code3, code4);
        setupAutoFocus(code4, code5);
        setupAutoFocus(code5, code6);

        verifyButton.setOnClickListener(v -> handleVerify());
        tvResendCode.setOnClickListener(v -> {
            if (canResend) resendCode();
            else Toast.makeText(this, "Vui lòng đợi trước khi gửi lại mã", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupAutoFocus(EditText current, EditText next) {
        current.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) next.requestFocus();
            }
            public void afterTextChanged(Editable s) {}
        });
    }

    private void handleVerify() {
        String code = code1.getText().toString() + code2.getText().toString() + code3.getText().toString()
                + code4.getText().toString() + code5.getText().toString() + code6.getText().toString();

        if (code.length() != 6) {
            Toast.makeText(this, "Vui lòng nhập đủ 6 số", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        if (code.equals(correctCode)) {
            setLoading(false);
            Toast.makeText(this, "Xác thực thành công!", Toast.LENGTH_SHORT).show();

            // Chuyển sang đặt lại mật khẩu
            Intent intent = new Intent(VerifyCodeActivity.this, ResetPasswordActivity.class);
            intent.putExtra("email", userEmail);
            startActivity(intent);
            finish();
        } else {
            setLoading(false);
            Toast.makeText(this, "Mã xác thực không đúng", Toast.LENGTH_SHORT).show();
            clearCode();
        }
    }

    private void clearCode() {
        code1.setText(""); code2.setText(""); code3.setText("");
        code4.setText(""); code5.setText(""); code6.setText("");
        code1.requestFocus();
    }

    private void startCountdown() {
        canResend = false;
        tvResendCode.setEnabled(false);
        tvTimer.setTextColor(getColor(R.color.text_gray));

        countDownTimer = new CountDownTimer(60000, 1000) {
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(String.format("(%02d:%02d)", seconds / 60, seconds % 60));
            }
            public void onFinish() {
                canResend = true;
                tvResendCode.setEnabled(true);
                tvResendCode.setTextColor(getColor(R.color.button_purple));
                tvTimer.setText("");
            }
        }.start();
    }

    private void resendCode() {
        Toast.makeText(this, "Mã mới: " + correctCode, Toast.LENGTH_LONG).show();
        startCountdown();
        clearCode();
    }

    private void setLoading(boolean isLoading) {
        if (progressBar != null)
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        verifyButton.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
