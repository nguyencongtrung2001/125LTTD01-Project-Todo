package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private Button loginButton;
    private TextView signupTextView, tvForgotPassword;
    private CheckBox rememberMeCheckbox;
    private ProgressBar progressBar;
    private ImageView togglePasswordVisibility;

    private DatabaseReference databaseReference;
    private boolean isPasswordVisible = false;

    // SharedPreferences keys
    private static final String PREFS_REMEMBER = "remember_credentials";
    private static final String KEY_REMEMBER_CHECKED = "remember_checked";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    // Simple encryption key (trong production nên dùng Android Keystore)
    private static final String ENCRYPTION_KEY = "ProjectTodoKey16"; // 16 bytes cho AES

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

        // Load thông tin đã lưu (nếu có)
        loadRememberedCredentials();

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
        rememberMeCheckbox = findViewById(R.id.rememberMeCheckbox);
        progressBar = findViewById(R.id.progressBar);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
    }

    private void setupListeners() {
        loginButton.setOnClickListener(v -> handleLogin());
        signupTextView.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class))
        );
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );

        // Toggle hiển thị mật khẩu
        togglePasswordVisibility.setOnClickListener(v -> togglePasswordVisibility());
    }

    /**
     * Chuyển đổi hiển thị/ẩn mật khẩu
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ẩn mật khẩu
            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            // Hiện mật khẩu
            passwordEditText.setInputType(android.text.InputType.TYPE_CLASS_TEXT |
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePasswordVisibility.setImageResource(R.drawable.ic_eye_on);
            isPasswordVisible = true;
        }
        // Đặt con trỏ về cuối text
        passwordEditText.setSelection(passwordEditText.getText().length());
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
                    // Lưu hoặc xóa thông tin đăng nhập
                    handleRememberMe(email, password);

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

    // ========== CHỨC NĂNG NHỚ MẬT KHẨU ==========

    /**
     * Load thông tin đã lưu (nếu có)
     */
    private void loadRememberedCredentials() {
        SharedPreferences prefs = getSharedPreferences(PREFS_REMEMBER, MODE_PRIVATE);
        boolean isRememberChecked = prefs.getBoolean(KEY_REMEMBER_CHECKED, false);

        rememberMeCheckbox.setChecked(isRememberChecked);

        if (isRememberChecked) {
            String savedEmail = prefs.getString(KEY_SAVED_EMAIL, "");
            String encryptedPassword = prefs.getString(KEY_SAVED_PASSWORD, "");

            if (!savedEmail.isEmpty() && !encryptedPassword.isEmpty()) {
                emailEditText.setText(savedEmail);

                // Giải mã mật khẩu
                String decryptedPassword = decryptPassword(encryptedPassword);
                if (decryptedPassword != null) {
                    passwordEditText.setText(decryptedPassword);
                }
            }
        }
    }

    /**
     * Xử lý lưu/xóa thông tin khi đăng nhập
     */
    private void handleRememberMe(String email, String password) {
        SharedPreferences prefs = getSharedPreferences(PREFS_REMEMBER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (rememberMeCheckbox.isChecked()) {
            // Lưu thông tin
            editor.putBoolean(KEY_REMEMBER_CHECKED, true);
            editor.putString(KEY_SAVED_EMAIL, email);

            // Mã hóa mật khẩu trước khi lưu
            String encryptedPassword = encryptPassword(password);
            if (encryptedPassword != null) {
                editor.putString(KEY_SAVED_PASSWORD, encryptedPassword);
            }
        } else {
            // Xóa thông tin đã lưu
            editor.putBoolean(KEY_REMEMBER_CHECKED, false);
            editor.remove(KEY_SAVED_EMAIL);
            editor.remove(KEY_SAVED_PASSWORD);
        }

        editor.apply();
    }

    /**
     * Mã hóa mật khẩu bằng AES
     */
    private String encryptPassword(String password) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encrypted = cipher.doFinal(password.getBytes());
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Giải mã mật khẩu
     */
    private String decryptPassword(String encryptedPassword) {
        try {
            SecretKeySpec key = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] decrypted = cipher.doFinal(Base64.decode(encryptedPassword, Base64.DEFAULT));
            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Xóa thông tin đã lưu (gọi khi đăng xuất)
     */
    public static void clearRememberedCredentials(android.content.Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_REMEMBER, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    // ========== CÁC HÀM KHÁC (giữ nguyên) ==========

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