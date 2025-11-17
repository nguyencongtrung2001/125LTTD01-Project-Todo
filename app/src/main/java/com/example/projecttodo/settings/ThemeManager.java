package com.example.projecttodo.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ThemeManager {

    private static final String PREFS_NAME = "settings";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences prefs;
    private final Context context;

    public ThemeManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return prefs.getBoolean(KEY_DARK_MODE, false);
    }

    private void setDarkModeToLocal(boolean isDark) {
        prefs.edit().putBoolean(KEY_DARK_MODE, isDark).apply();
    }

    // Cập nhật lên Firebase (bất đồng bộ)
    private void updateFirebase(boolean isDark, Runnable onComplete) {
        SharedPreferences sessionPrefs = context.getSharedPreferences("user_session", 0);
        String userId = sessionPrefs.getString("user_id", "");

        if (userId == null || userId.isEmpty()) {
            onComplete.run();
            return;
        }

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("settings")
                .child("darkMode");

        ref.setValue(isDark).addOnCompleteListener(task -> onComplete.run());
    }

    // Áp dụng theme + luôn mở lại tab Settings
    public void applyTheme(Activity activity, boolean isDark) {

        setDarkModeToLocal(isDark);

        // LƯU tab hiện tại = settings
        SharedPreferences nav = context.getSharedPreferences("nav_state", 0);
        nav.edit().putString("last_tab", "settings").apply();

        updateFirebase(isDark, () -> {
            if (activity instanceof com.example.projecttodo.HomeActivity) {
                ((com.example.projecttodo.HomeActivity) activity).recreateWithFade(isDark);
            } else {
                activity.recreate();
            }
        });
    }
}
