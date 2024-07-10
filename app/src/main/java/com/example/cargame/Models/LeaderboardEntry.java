package com.example.cargame.Models;

public class LeaderboardEntry {
    private String playerName;
    private int combinedScore;
    private double latitude;
    private double longitude;

    public LeaderboardEntry(String playerName, int combinedScore, double latitude, double longitude) {
        this.playerName = playerName;
        this.combinedScore = combinedScore;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getCombinedScore() { return combinedScore; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}