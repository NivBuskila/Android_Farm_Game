package com.example.cargame;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.cargame.Logic.GameManager;
import com.example.cargame.Managers.LeaderboardManager;
import com.example.cargame.Models.LeaderboardEntry;
import com.example.cargame.Utilities.SoundPlayer;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final int ROWS = 8;
    private static final int COLS = 5;
    private static final int LIVES = 3;
    private static final long UPDATE_DELAY = 455;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private static final String TAG = "MainActivity";

    private GameManager gameManager;
    private FrameLayout[] lanes;
    private AppCompatImageView[] hearts;
    private TextView combinedScoreTextView;

    private boolean timerOn = false;
    private Timer timer;
    private SoundPlayer soundPlayer;
    private LeaderboardManager leaderboardManager;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private double userLatitude = 0.0;
    private double userLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        soundPlayer = new SoundPlayer(this);
        gameManager = new GameManager(ROWS, COLS, LIVES, soundPlayer, this);
        leaderboardManager = new LeaderboardManager();
        leaderboardManager.loadEntries(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeViews();
        checkLocationPermission();
        checkLocationServices();
    }

    private void initializeViews() {
        lanes = new FrameLayout[]{
                findViewById(R.id.first_Lane),
                findViewById(R.id.second_Lane),
                findViewById(R.id.thired_Lane),
                findViewById(R.id.fourth_Lane),
                findViewById(R.id.fived_Lane)
        };

        hearts = new AppCompatImageView[]{
                findViewById(R.id.main_IMG_heart1),
                findViewById(R.id.main_IMG_heart2),
                findViewById(R.id.main_IMG_heart3)
        };

        combinedScoreTextView = findViewById(R.id.combined_score_text);

        FloatingActionButton leftButton = findViewById(R.id.left_ARROW_btn);
        FloatingActionButton rightButton = findViewById(R.id.right_ARROW_btn);

        leftButton.setOnClickListener(v -> gameManager.moveTractor(true));
        rightButton.setOnClickListener(v -> gameManager.moveTractor(false));

        for (FrameLayout lane : lanes) {
            lane.removeAllViews();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }
    }

    private void startLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        userLatitude = location.getLatitude();
                        userLongitude = location.getLongitude();
                        Log.d(TAG, "Location updated: " + userLatitude + ", " + userLongitude);
                    }
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    private void checkLocationServices() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            new AlertDialog.Builder(this)
                    .setMessage("You need to enable location services to save the location in the high score board")
                    .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        stopLocationUpdates();
    }

    private void startTimer() {
        Log.d(TAG, "startTimer: Timer Started");
        if (!timerOn) {
            timerOn = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(() -> {
                        gameManager.update();
                        updateUI();
                        if (gameManager.isGameOver()) {
                            showGameOverDialog();
                        }
                    });
                }
            }, 0, UPDATE_DELAY);
        }
    }

    private void stopTimer() {
        Log.d(TAG, "stopTimer: Timer Stopped");
        if (timerOn) {
            timerOn = false;
            timer.cancel();
        }
    }

    private void updateUI() {
        int[][] logicalGrid = gameManager.getGrid();
        for (int i = 0; i < COLS; i++) {
            lanes[i].removeAllViews();
            for (int j = 0; j < ROWS; j++) {
                if (logicalGrid[j][i] != GameManager.CLEAR) {
                    AppCompatImageView imageView = new AppCompatImageView(this);
                    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                    );

                    int objectSize = lanes[i].getWidth() / 2;
                    params.width = objectSize;
                    params.height = objectSize;

                    params.topMargin = j * (lanes[i].getHeight() / ROWS);
                    params.leftMargin = (lanes[i].getWidth() - objectSize) / 2;
                    imageView.setLayoutParams(params);

                    switch (logicalGrid[j][i]) {
                        case GameManager.TRACTOR:
                            imageView.setImageResource(R.drawable.tractor);
                            break;
                        case GameManager.BLOCK:
                            imageView.setImageResource(R.drawable.scarecrow);
                            break;
                        case GameManager.CORN:
                            imageView.setImageResource(R.drawable.corn);
                            break;
                    }

                    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    lanes[i].addView(imageView);
                }
            }
        }

        combinedScoreTextView.setText(getString(R.string.combined_score_format, gameManager.getCombinedScore()));
        updateLives();
    }

    private void updateLives() {
        int lives = gameManager.getLives();
        for (int i = 0; i < hearts.length; i++) {
            hearts[i].setVisibility(i < lives ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void showGameOverDialog() {
        stopTimer();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Game Over");
        builder.setMessage("Your score is: " + gameManager.getCombinedScore() + ". Enter your name:");

        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String playerName = input.getText().toString();
                if (!playerName.isEmpty()) {
                    saveScore(playerName);
                }
                finish();
            }
        });

        builder.setCancelable(false);
        builder.show();
    }

    private void saveScore(String playerName) {
        int combinedScore = gameManager.getCombinedScore();
        LeaderboardEntry newEntry = new LeaderboardEntry(playerName, combinedScore, userLatitude, userLongitude);
        leaderboardManager.addEntry(newEntry);
        leaderboardManager.saveEntries(this);
        Log.d(TAG, "Score saved: " + playerName + " - " + combinedScore + " at " + userLatitude + ", " + userLongitude);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.d(TAG, "Location permission denied");
            }
        }
    }

    private void stopLocationUpdates() {
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}