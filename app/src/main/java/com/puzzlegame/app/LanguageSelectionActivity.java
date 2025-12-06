package com.puzzlegame.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LanguageSelectionActivity extends AppCompatActivity {

    private PreferenceManager preferenceManager;
    private SoundManager soundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceManager = new PreferenceManager(this);
        soundManager = new SoundManager();

        if (!preferenceManager.isFirstLaunch()) {
            Utils.setLocale(this, preferenceManager.getLanguage());
            startMainActivity();
            return;
        }

        setContentView(R.layout.activity_language_selection);

        TextView titleText = findViewById(R.id.titleText);
        Button btnTurkish = findViewById(R.id.btnTurkish);
        Button btnEnglish = findViewById(R.id.btnEnglish);

        titleText.setText(R.string.puzzle_logo);

        btnTurkish.setOnClickListener(v -> {
            soundManager.playTap();
            selectLanguage("tr");
        });

        btnEnglish.setOnClickListener(v -> {
            soundManager.playTap();
            selectLanguage("en");
        });
    }

    private void selectLanguage(String languageCode) {
        preferenceManager.setLanguage(languageCode);
        preferenceManager.setFirstLaunch(false);
        Utils.setLocale(this, languageCode);
        startMainActivity();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
