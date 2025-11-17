package com.example.projecttodo.settings;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.projecttodo.R;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ChangePasswordDialog {

    private final Context context;

    public ChangePasswordDialog(Context context) {
        this.context = context;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.settings_dialog_change_password);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_dialog_round);
        dialog.setCancelable(true);

        EditText edtOld = dialog.findViewById(R.id.edt_old_password);
        EditText edtNew = dialog.findViewById(R.id.edt_new_password);
        EditText edtConfirm = dialog.findViewById(R.id.edt_confirm_password);

        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {

            String oldPass = edtOld.getText().toString().trim();
            String newPass = edtNew.getText().toString().trim();
            String confirmPass = edtConfirm.getText().toString().trim();

            if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPass.equals(confirmPass)) {
                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lấy userId đã đăng nhập
            SharedPreferences session = context.getSharedPreferences("user_session", 0);
            String userId = session.getString("user_id", "");

            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("password")
                    .get()
                    .addOnCompleteListener(task -> {

                        if (!task.isSuccessful()) {
                            Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String currentHash = task.getResult().getValue(String.class);
                        String oldHash = hashPassword(oldPass);

                        if (currentHash == null) {
                            Toast.makeText(context, "Không tìm thấy mật khẩu hiện tại", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (!oldHash.equals(currentHash)) {
                            Toast.makeText(context, "Mật khẩu cũ không đúng", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Hash mật khẩu mới
                        String newHash = hashPassword(newPass);

                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(userId)
                                .child("password")
                                .setValue(newHash)
                                .addOnCompleteListener(updateTask -> {

                                    if (updateTask.isSuccessful()) {
                                        Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    } else {
                                        Toast.makeText(context, "Đổi mật khẩu thất bại", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
        });

        dialog.show();
        // Làm dialog rộng 90% màn hình
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.90),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

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
}
