package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.SwitchCompat;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark;
    private SwitchCompat switchNotification;
    private CheckBox cbSilentMode;
    private TextView tvSilentTime, tvLanguage, tvVersion, tvEmail;
    private Button btnLogout;

    private SharedPreferences prefs;

    public SettingsFragment() { }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        rgTheme = view.findViewById(R.id.rg_theme);
        rbLight = view.findViewById(R.id.rb_light);
        rbDark = view.findViewById(R.id.rb_dark);
        switchNotification = view.findViewById(R.id.switch_notification);
        cbSilentMode = view.findViewById(R.id.cb_silent_mode);
        tvSilentTime = view.findViewById(R.id.tv_silent_time);
        tvLanguage = view.findViewById(R.id.tv_language);
        tvVersion = view.findViewById(R.id.tv_version);
        tvEmail = view.findViewById(R.id.tv_email);
        btnLogout = view.findViewById(R.id.btn_logout);

        prefs = requireContext().getSharedPreferences("settings", 0);
        loadSettings();

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isDark = (checkedId == R.id.rb_dark);
            saveTheme(isDark);
            Toast.makeText(getContext(),
                    isDark ? getString(R.string.theme_dark) : getString(R.string.theme_light),
                    Toast.LENGTH_SHORT).show();
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notification", isChecked).apply();
            Toast.makeText(getContext(),
                    isChecked ? getString(R.string.notifications_on) : getString(R.string.notifications_off),
                    Toast.LENGTH_SHORT).show();
        });

        cbSilentMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvSilentTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            prefs.edit().putBoolean("silent", isChecked).apply();
        });

        tvLanguage.setOnClickListener(v ->
                Toast.makeText(getContext(), getString(R.string.language_coming_soon), Toast.LENGTH_SHORT).show()
        );

        btnLogout.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    getString(R.string.logout_success),
                    Toast.LENGTH_SHORT
            ).show();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        return view;
    }

    private void loadSettings() {
        boolean isDark = prefs.getBoolean("dark_mode", false);
        boolean isNoti = prefs.getBoolean("notification", true);
        boolean isSilent = prefs.getBoolean("silent", false);

        rbDark.setChecked(isDark);
        rbLight.setChecked(!isDark);
        switchNotification.setChecked(isNoti);
        cbSilentMode.setChecked(isSilent);
        tvSilentTime.setVisibility(isSilent ? View.VISIBLE : View.GONE);
    }

    private void saveTheme(boolean isDark) {
        prefs.edit().putBoolean("dark_mode", isDark).apply();

        SharedPreferences navPrefs = requireContext().getSharedPreferences("nav_state", 0);
        navPrefs.edit().putString("last_tab", "settings").apply();

        if (getActivity() instanceof HomeActivity) {
            ((HomeActivity) getActivity()).recreateWithFade(isDark);
        } else {
            requireActivity().recreate();
        }
    }
}
