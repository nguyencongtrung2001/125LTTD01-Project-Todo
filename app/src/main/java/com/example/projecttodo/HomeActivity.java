package com.example.projecttodo;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
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

        // Khởi tạo views
        initViews();

        // Khởi tạo Fragment Manager
        fragmentManager = getSupportFragmentManager();

        // Load fragment mặc định
        loadFragment(new TaskListFragment());

        // Setup Bottom Navigation
        setupBottomNavigation();
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

        // Reset tất cả icons và labels về màu mặc định
        iconHome.setColorFilter(getResources().getColor(R.color.icon_gray));
        iconCalendar.setColorFilter(getResources().getColor(R.color.icon_gray));
        iconStatistics.setColorFilter(getResources().getColor(R.color.icon_gray));
        iconSettings.setColorFilter(getResources().getColor(R.color.icon_gray));

        labelHome.setTextColor(getResources().getColor(R.color.icon_gray));
        labelCalendar.setTextColor(getResources().getColor(R.color.icon_gray));
        labelStatistics.setTextColor(getResources().getColor(R.color.icon_gray));
        labelSettings.setTextColor(getResources().getColor(R.color.icon_gray));

        // Highlight tab được chọn
        switch (tab) {
            case "home":
                iconHome.setColorFilter(getResources().getColor(R.color.white));
                labelHome.setTextColor(getResources().getColor(R.color.white));
                break;
            case "calendar":
                iconCalendar.setColorFilter(getResources().getColor(R.color.white));
                labelCalendar.setTextColor(getResources().getColor(R.color.white));
                break;
            case "statistics":
                iconStatistics.setColorFilter(getResources().getColor(R.color.white));
                labelStatistics.setTextColor(getResources().getColor(R.color.white));
                break;
            case "settings":
                iconSettings.setColorFilter(getResources().getColor(R.color.white));
                labelSettings.setTextColor(getResources().getColor(R.color.white));
                break;
        }
    }
}