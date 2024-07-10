package com.example.cargame.Managers;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.cargame.Models.LeaderboardEntry;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class LeaderboardManager {
    private static final int MAX_ENTRIES = 10;
    private static final String PREFS_NAME = "CarGamePrefs";
    private static final String TOP_SCORES_KEY = "TopScores";
    private List<LeaderboardEntry> entries;

    public LeaderboardManager() {
        entries = new ArrayList<>();
    }

    public void addEntry(LeaderboardEntry entry) {
        entries.add(entry);
        sortEntries();
        if (entries.size() > MAX_ENTRIES) {
            entries.remove(entries.size() - 1);
        }
    }

    private void sortEntries() {
        Collections.sort(entries, new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry e1, LeaderboardEntry e2) {
                return Integer.compare(e2.getCombinedScore(), e1.getCombinedScore());
            }
        });
    }

    public List<LeaderboardEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public void saveEntries(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(entries);
        editor.putString(TOP_SCORES_KEY, json);
        editor.apply();
    }

    public void loadEntries(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString(TOP_SCORES_KEY, null);
        Type type = new TypeToken<ArrayList<LeaderboardEntry>>() {}.getType();
        entries = gson.fromJson(json, type);
        if (entries == null) {
            entries = new ArrayList<>();
        }
    }
}
