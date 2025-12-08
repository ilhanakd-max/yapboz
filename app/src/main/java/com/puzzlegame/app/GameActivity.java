package com.puzzlegame.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // This activity is reserved for future full-screen game mode
        // Currently, the game runs in MainActivity
    }
}
