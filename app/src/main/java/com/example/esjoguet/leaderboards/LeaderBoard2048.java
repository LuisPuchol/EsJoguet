package com.example.esjoguet.leaderboards;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.esjoguet.Activity_LeaderBoard;
import com.example.esjoguet.DBAssistant;
import com.example.esjoguet.R;

import java.util.List;

public class LeaderBoard2048 extends AppCompatActivity {
    private RecyclerView recyclerLeaderboard2048;
    private LeaderBoardAdapter2048 adapter;
    private List<LeaderBoardEntry2048> leaderboardEntries;
    private DBAssistant dbAssistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startStuff(savedInstanceState);

        recyclerLeaderboard2048 = findViewById(R.id.recyclerLeaderboard2048);
        recyclerLeaderboard2048.setLayoutManager(new LinearLayoutManager(this));

        dbAssistant = new DBAssistant(this);
        leaderboardEntries = dbAssistant.get2048Leaderboard();

        adapter = new LeaderBoardAdapter2048(leaderboardEntries);
        recyclerLeaderboard2048.setAdapter(adapter);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(LeaderBoard2048.this, Activity_LeaderBoard.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_leader_board2048);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}