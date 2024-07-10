package com.example.cargame;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.cargame.Managers.LeaderboardManager;
import com.example.cargame.Models.LeaderboardEntry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardActivity extends AppCompatActivity implements OnMapReadyCallback {

    private LeaderboardManager leaderboardManager;
    private ListView leaderboardListView;
    private GoogleMap mMap;
    private List<LeaderboardEntry> entries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leaderboard);

        leaderboardManager = new LeaderboardManager();
        leaderboardListView = findViewById(R.id.leaderboard_list);

        leaderboardManager.loadEntries(this);
        entries = leaderboardManager.getEntries();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateLeaderboardList();
    }

    private void updateLeaderboardList() {
        List<String> displayList = new ArrayList<>();
        for (LeaderboardEntry entry : entries) {
            displayList.add(entry.getPlayerName() + " - " + entry.getCombinedScore());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, displayList);
        leaderboardListView.setAdapter(adapter);

        leaderboardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showLocationOnMap(entries.get(position));
            }
        });
    }

    private void showLocationOnMap(LeaderboardEntry entry) {
        if (mMap != null) {
            LatLng location = new LatLng(entry.getLatitude(), entry.getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(location).title(entry.getPlayerName()));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }
}