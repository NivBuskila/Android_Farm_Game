package com.example.cargame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    private static final String EXTRA_GAME_PACE = "EXTRA_GAME_PACE";
    private static final int PACE_SLOW = 0;
    private static final int PACE_FAST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        Button leaderboardButton = findViewById(R.id.leaderboard_button);
        Button startGameButton = findViewById(R.id.start_game_button);
        Button startTiltGameButton = findViewById(R.id.start_tilt_game_button);
        RadioGroup paceGroup = findViewById(R.id.pace_group);

        startGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectedPace = (paceGroup.getCheckedRadioButtonId() == R.id.radio_fast) ? PACE_FAST : PACE_SLOW;
                startGame(selectedPace);
            }
        });

        startTiltGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTiltGame();
            }
        });

        leaderboardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLeaderboard();
            }
        });

    }

    private void openLeaderboard() {
        Intent intent = new Intent(MenuActivity.this, LeaderboardActivity.class);
        startActivity(intent);
    }
    private void startGame(int pace) {
        Intent intent = new Intent(MenuActivity.this, MainActivity.class);
        intent.putExtra(EXTRA_GAME_PACE, pace);
        startActivity(intent);
    }

    private void startTiltGame() {
        Intent intent = new Intent(MenuActivity.this, TiltControlActivity.class);
        startActivity(intent);
    }

}