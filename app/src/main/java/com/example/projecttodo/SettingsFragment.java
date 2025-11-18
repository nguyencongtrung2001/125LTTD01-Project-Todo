package com.example.projecttodo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.example.projecttodo.settings.ChangePasswordDialog;
import com.example.projecttodo.settings.ThemeManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SettingsFragment extends Fragment {

    private RadioGroup rgTheme;
    private RadioButton rbLight, rbDark;
    private SwitchCompat switchNotification;
    private CheckBox cbSilentMode;
    private TextView tvSilentTime, tvVersion;
    private TextView tvEmail, tvName, tvCreatedAt;
    private Spinner spinnerLanguage;
    private Button btnLogout, btnChangePassword;

    private SharedPreferences prefs;
    private ThemeManager themeManager;

    private String userId;
    private DatabaseReference userRef;

    private static final String TAG = "SettingsFragment";

    public SettingsFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        initViews(view);
        prefs = requireContext().getSharedPreferences("settings", 0);
        themeManager = new ThemeManager(requireContext());

        loadUserSession();
        loadUserInfoFromFirebase();
        loadSettings();
        setupLanguageSpinner();
        setupListeners();

        return view;
    }

    private void initViews(View view) {

        tvEmail = view.findViewById(R.id.tv_email);
        tvName = view.findViewById(R.id.tv_name);
        tvCreatedAt = view.findViewById(R.id.tv_createAt);

        rgTheme = view.findViewById(R.id.rg_theme);
        rbLight = view.findViewById(R.id.rb_light);
        rbDark = view.findViewById(R.id.rb_dark);

        switchNotification = view.findViewById(R.id.switch_notification);
        cbSilentMode = view.findViewById(R.id.cb_silent_mode);
        tvSilentTime = view.findViewById(R.id.tv_silent_time);

        spinnerLanguage = view.findViewById(R.id.spinner_language);

        tvVersion = view.findViewById(R.id.tv_version);

        btnChangePassword = view.findViewById(R.id.btn_change_password);
        btnLogout = view.findViewById(R.id.btn_logout);
    }

    private void setupLanguageSpinner() {

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.languages,
                android.R.layout.simple_spinner_item
        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Load saved language
        String savedLang = prefs.getString("language", "Tiếng Việt");
        int position = adapter.getPosition(savedLang);
        spinnerLanguage.setSelection(position);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String lang = parent.getItemAtPosition(pos).toString();
                prefs.edit().putString("language", lang).apply();
            }

            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void loadUserSession() {
        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("user_session", 0);
        userId = sessionPrefs.getString("user_id", "");

        userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(userId)
                .child("info");
    }

    private void loadUserInfoFromFirebase() {

        userRef.get().addOnCompleteListener(task -> {

            if (!task.isSuccessful()) {
                Log.e(TAG, "Error loading user info");
                return;
            }

            DataSnapshot s = task.getResult();
            if (s == null) return;

            String email = s.child("email").getValue(String.class);
            String fullname = s.child("name").getValue(String.class);
            Long createdAt = s.child("createdAt").getValue(Long.class);

            if (email != null) tvEmail.setText(email);
            if (fullname != null) tvName.setText(fullname);

            if (createdAt != null) {
                String formatted = android.text.format.DateFormat.format("dd/MM/yyyy  HH:mm", createdAt).toString();
                tvCreatedAt.setText(formatted);
            }
        });
    }

    private void loadSettings() {

        boolean isDark = themeManager.isDarkMode();
        boolean isNoti = prefs.getBoolean("notification", true);
        boolean isSilent = prefs.getBoolean("silent", false);

        rbDark.setChecked(isDark);
        rbLight.setChecked(!isDark);

        switchNotification.setChecked(isNoti);
        cbSilentMode.setChecked(isSilent);

        tvSilentTime.setVisibility(isSilent ? View.VISIBLE : View.GONE);
    }

    private void setupListeners() {

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isDark = (checkedId == R.id.rb_dark);

            themeManager.applyTheme(requireActivity(), isDark);

            Toast.makeText(getContext(),
                    isDark ? "Chế độ tối" : "Chế độ sáng",
                    Toast.LENGTH_SHORT).show();
        });

        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("notification", isChecked).apply();
        });

        cbSilentMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            tvSilentTime.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            prefs.edit().putBoolean("silent", isChecked).apply();
        });

        btnLogout.setOnClickListener(v -> handleLogout());

        btnChangePassword.setOnClickListener(v -> {
            new ChangePasswordDialog(requireContext()).show();
        });
    }

    private void handleLogout() {

        SharedPreferences sessionPrefs = requireContext().getSharedPreferences("user_session", 0);
        sessionPrefs.edit().clear().apply();

        LoginActivity.clearRememberedCredentials(requireContext());

        SharedPreferences navPrefs = requireContext().getSharedPreferences("nav_state", 0);
        navPrefs.edit().clear().apply();

        Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
