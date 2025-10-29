package com.example.projecttodo;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsetsController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeActivity extends AppCompatActivity {

    private LinearLayout navHome, navCalendar, navStatistics, navSettings;
    private ImageView iconHome, iconCalendar, iconStatistics, iconSettings;
    private TextView labelHome, labelCalendar, labelStatistics, labelSettings;

    private FragmentManager fragmentManager;
    private String currentTab = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initViews();
        fragmentManager = getSupportFragmentManager();

        // Khôi phục tab trước đó
        SharedPreferences prefs = getSharedPreferences("nav_state", MODE_PRIVATE);
        String lastTab = prefs.getString("last_tab", "home");

        switch (lastTab) {
            case "calendar":
                loadFragment(new CalendarFragment());
                selectTab("calendar");
                break;
            case "statistics":
                loadFragment(new StatisticsFragment());
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

        // Cập nhật lại màu icon hệ thống (SIM, pin, navigation)
        updateSystemBarIcons();
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
                loadFragment(new StatisticsFragment());
                selectTab("statistics");
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

        // Lấy màu động từ theme hiện tại
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();

        // Màu nhấn theo theme (colorBottomNavigationBar)
        theme.resolveAttribute(R.attr.colorBottomNavigationBar, typedValue, true);
        int colorActive = typedValue.data;

        // Màu xám cho icon & text chưa chọn
        int colorInactive = ContextCompat.getColor(this, R.color.icon_gray);

        // Reset tất cả icon & text về màu inactive
        iconHome.setColorFilter(colorInactive);
        iconCalendar.setColorFilter(colorInactive);
        iconStatistics.setColorFilter(colorInactive);
        iconSettings.setColorFilter(colorInactive);

        labelHome.setTextColor(colorInactive);
        labelCalendar.setTextColor(colorInactive);
        labelStatistics.setTextColor(colorInactive);
        labelSettings.setTextColor(colorInactive);

        // Áp dụng màu active cho tab đang chọn
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


    // ===============================
    // Hiệu ứng chuyển theme mượt (Overlay Fade)
    // ===============================
    public void recreateWithFade(boolean isDark) {
        final View decor = getWindow().getDecorView();
        final View overlay = new View(this);

        // Luôn chớp màu đen nhẹ dù giao diện sáng hay tối
        overlay.setBackgroundColor(getColor(android.R.color.black));
        overlay.setAlpha(0f);

        ((ViewGroup) decor).addView(overlay, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Fade in (từ trong suốt -> đen)
        overlay.animate()
                .alpha(1f)
                .setDuration(150)
                .withEndAction(() -> {
                    runOnUiThread(() -> {
                        // Đổi chế độ sáng/tối
                        AppCompatDelegate.setDefaultNightMode(
                                isDark ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                        );

                        // Tạo lại Activity
                        super.recreate();

                        // Fade out (từ đen -> trong suốt)
                        overlay.animate()
                                .alpha(0f)
                                .setDuration(300)
                                .withEndAction(() -> {
                                    ((ViewGroup) decor).removeView(overlay);
                                    // Cập nhật lại icon thanh hệ thống sau khi recreate
                                    updateSystemBarIcons();
                                })
                                .start();
                    });
                })
                .start();
    }

    // ===============================
    // Tự đổi màu icon thanh trạng thái & điều hướng
    // ===============================
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
