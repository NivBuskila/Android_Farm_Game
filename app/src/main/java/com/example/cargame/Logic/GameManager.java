package com.example.cargame.Logic;

import com.example.cargame.R;
import com.example.cargame.Utilities.SoundPlayer;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.widget.Toast;

import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken;
import java.util.Random;

public class GameManager {
    public static final int CLEAR = 0;
    public static final int TRACTOR = 1;
    public static final int BLOCK = 2;
    public static final int CORN = 3;
    private static final String PREFS_NAME = "CarGamePrefs";
    private static final String TOP_SCORES_KEY = "TopScores";
    private int[][] grid;
    private int rows;
    private int cols;
    private int tractorColumn;
    private int score;
    private int lives;
    private int distance;
    private Random random;
    private SoundPlayer soundPlayer;
    private int combinedScore;
    private static final int DISTANCE_WEIGHT = 1;
    private static final int SCORE_WEIGHT = 10;
    private Context context;
    private Vibrator vibrator;

    public GameManager(int rows, int cols, int lives, SoundPlayer soundPlayer, Context context) {
        this.rows = rows;
        this.cols = cols;
        this.lives = lives;
        this.grid = new int[rows][cols];
        this.random = new Random();
        this.soundPlayer = soundPlayer;
        this.context = context;
        this.vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        resetGame();
    }

    public void resetGame() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = CLEAR;
            }
        }
        tractorColumn = cols / 2;
        grid[rows - 1][tractorColumn] = TRACTOR;
        score = 0;
        lives = 3;
        distance = 0;
    }

    public void update() {
        moveObstaclesDown();
        spawnObject();
        checkCollisions();
        distance++;
        updateCombinedScore();
    }

    private void updateCombinedScore() {
        combinedScore = (distance * DISTANCE_WEIGHT) + (score * SCORE_WEIGHT);
    }

    public int getCombinedScore() {
        return combinedScore;
    }

    private void moveObstaclesDown() {
        for (int i = rows - 1; i > 0; i--) {
            System.arraycopy(grid[i - 1], 0, grid[i], 0, cols);
        }
        for (int j = 0; j < cols; j++) {
            grid[0][j] = CLEAR;
        }
    }

    private void spawnObject() {
        if (random.nextInt(100) < 40) {
            int column = random.nextInt(cols);
            grid[0][column] = random.nextInt(100) < 70 ? BLOCK : CORN;
        }
    }

    private void checkCollisions() {
        if (grid[rows - 1][tractorColumn] == BLOCK) {
            lives--;
            soundPlayer.playSound(R.raw.crash_sound);
            Toast.makeText(context, "Crash!", Toast.LENGTH_SHORT).show();
            vibrator.vibrate(500);
        } else if (grid[rows - 1][tractorColumn] == CORN) {
            soundPlayer.playSound(R.raw.success_sound);
            score += 10;
        }
        grid[rows - 1][tractorColumn] = TRACTOR;
    }

    public void moveTractor(boolean moveLeft) {
        grid[rows - 1][tractorColumn] = CLEAR;
        if (moveLeft && tractorColumn > 0) {
            tractorColumn--;
        } else if (!moveLeft && tractorColumn < cols - 1) {
            tractorColumn++;
        }
        grid[rows - 1][tractorColumn] = TRACTOR;
    }

    public int[][] getGrid() {
        return grid;
    }

    public int getScore() {
        return score;
    }

    public int getLives() {
        return lives;
    }

    public int getDistance() {
        return distance;
    }

    public boolean isGameOver() {
        return lives <= 0;
    }
}