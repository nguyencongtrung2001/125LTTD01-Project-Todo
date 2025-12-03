package com.example.projecttodo;

import android.Manifest;                              // <<< thêm
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.pm.PackageManager;             // <<< thêm
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;                   // <<< thêm (cho onRequestPermissionsResult)
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;              // <<< thêm
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.projecttodo.ThongKe.StatisticsFragment;
import com.example.projecttodo.ThongKe.StatisticsUpdater;

public class HomeActivity extends AppCompatActivity {

    // ====== THÊM: request code xin quyền thông báo ======
    private static final int REQ_POST_NOTIF = 1001;   // <<< thêm

    private LinearLayout navHome, navCalendar, navStatistics, navSettings;
    private ImageView iconHome, iconCalendar, iconStatistics, iconSettings;
    private TextView labelHome, labelCalendar, labelStatistics, labelSettings;

    private FragmentManager fragmentManager;
    private String currentTab = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        AppCompatDelegate.setDefaultNightMode(
                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ensureNotificationPermission();
        initViews();
        fragmentManager = getSupportFragmentManager();

        SharedPreferences navPrefs = getSharedPreferences("nav_state", MODE_PRIVATE);
        String lastTab = navPrefs.getString("last_tab", "home");

        switch (lastTab) {
            case "calendar":
                loadFragment(new CalendarFragment());
                selectTab("calendar");
                break;
            case "statistics":
                openStatisticsTab();  // <-- update trước rồi mới mở
                selectTab("statistics");
                break;
            case "settings":
                loadFragment(new SettingsFragment());
                selectTab("settings");
                break;
            default:
                loadFragment(new TaskListFragment());
                selectTab("home");
                break;
        }

        setupBottomNavigation();
        updateSystemBarIcons();
    }

    // ====== THÊM: hàm kiểm tra/xin quyền thông báo ======
    private void ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIF
                );
            }
        }
    }

    // ====== THÊM: nhận kết quả xin quyền ======
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_POST_NOTIF) {
            boolean granted = grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
            }
        }
    }

    private void initViews() {
        navHome = findViewById(R.id.navHome);
        navCalendar = findViewById(R.id.navCalendar);
        navStatistics = findViewById(R.id.navStatistics);
        navSettings = findViewById(R.id.navSettings);

        iconHome = findViewById(R.id.iconHome);
        iconCalendar = findViewById(R.id.iconCalendar);
        iconStatistics = findViewById(R.id.iconStatistics);
        iconSettings = findViewById(R.id.iconSettings);

        labelHome = findViewById(R.id.labelHome);
        labelCalendar = findViewById(R.id.labelCalendar);
        labelStatistics = findViewById(R.id.labelStatistics);
        labelSettings = findViewById(R.id.labelSettings);
    }

    private void setupBottomNavigation() {
        navHome.setOnClickListener(v -> {
            if (!currentTab.equals("home")) {
                loadFragment(new TaskListFragment());
                selectTab("home");
            }
        });

        navCalendar.setOnClickListener(v -> {
            if (!currentTab.equals("calendar")) {
                loadFragment(new CalendarFragment());
                selectTab("calendar");
            }
        });

        navStatistics.setOnClickListener(v -> {
            if (!currentTab.equals("statistics")) {
                openStatisticsTab();
            }
        });

        navSettings.setOnClickListener(v -> {
            if (!currentTab.equals("settings")) {
                loadFragment(new SettingsFragment());
                selectTab("settings");
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.commit();
    }

    private void selectTab(String tab) {
        currentTab = tab;

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();

        theme.resolveAttribute(R.attr.colorBottomNavigationBar, typedValue, true);
        int colorActive = typedValue.data;

        int colorInactive = ContextCompat.getColor(this, R.color.icon_gray);

        iconHome.setColorFilter(colorInactive);
        iconCalendar.setColorFilter(colorInactive);
        iconStatistics.setColorFilter(colorInactive);
        iconSettings.setColorFilter(colorInactive);

        labelHome.setTextColor(colorInactive);
        labelCalendar.setTextColor(colorInactive);
        labelStatistics.setTextColor(colorInactive);
        labelSettings.setTextColor(colorInactive);

        switch (tab) {
            case "home":
                iconHome.setColorFilter(colorActive);
                labelHome.setTextColor(colorActive);
                break;
            case "calendar":
                iconCalendar.setColorFilter(colorActive);
                labelCalendar.setTextColor(colorActive);
                break;
            case "statistics":
                iconStatistics.setColorFilter(colorActive);
                labelStatistics.setTextColor(colorActive);
                break;
            case "settings":
                iconSettings.setColorFilter(colorActive);
                labelSettings.setTextColor(colorActive);
                break;
        }
    }

    private void openStatisticsTab() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("user_id", "");

        if (userId == null || userId.isEmpty()) {
            loadFragment(new StatisticsFragment());
            selectTab("statistics");
            return;
        }

        StatisticsUpdater updater = new StatisticsUpdater(userId);
        updater.updateStatistics(task -> {
            loadFragment(new StatisticsFragment());
            selectTab("statistics");
        });
    }

    public void recreateWithFade(boolean isDark) {
        final View decor = getWindow().getDecorView();
        final View overlay = new View(this);

        overlay.setBackgroundColor(getColor(android.R.color.black));
        overlay.setAlpha(0f);

        ((ViewGroup) decor).addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        overlay.animate()
                .alpha(1f)
                .setDuration(150)
                .withEndAction(() -> {
                    runOnUiThread(() -> {

                        AppCompatDelegate.setDefaultNightMode(
                                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                        );

                        super.recreate();

                        overlay.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    ((ViewGroup) decor).removeView(overlay);
                                    updateSystemBarIcons();
                                })
                                .start();
                    });
                })
                .start();
    }

    private void updateSystemBarIcons() {
        View decorView = getWindow().getDecorView();
        boolean isDarkMode = (getResources().getConfiguration().uiMode &
                android.content.res.Configuration.UI_MODE_NIGHT_MASK)
                == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.setSystemBarsAppearance(
                        isDarkMode ? 0 :
                                (WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS),
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                                | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS
                );
            }
        } else {
            int flags = decorView.getSystemUiVisibility();
            if (isDarkMode) {
                flags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            } else {
                flags |= (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            decorView.setSystemUiVisibility(flags);
        }
    }
}
