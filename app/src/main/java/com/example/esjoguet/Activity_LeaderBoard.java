package com.example.esjoguet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.esjoguet.leaderboards.LeaderBoard2048;
import com.example.esjoguet.leaderboards.LeaderBoardStacker;

public class Activity_LeaderBoard extends AppCompatActivity {
    private Button button_game_2048, button_game_stacker;;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);
        startStuff(savedInstanceState);

        button_game_2048 = findViewById(R.id.Button2048);
        button_game_stacker = findViewById(R.id.ButtonStacker);


        button_game_2048.setOnClickListener((View v) -> {
            Intent intent = new Intent(Activity_LeaderBoard.this, LeaderBoard2048.class);
            startActivity(intent);
        });

        button_game_stacker.setOnClickListener((View v) -> {
            Intent intent = new Intent(Activity_LeaderBoard.this, LeaderBoardStacker.class);
            startActivity(intent);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Activity_LeaderBoard.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });
    }

    private void startStuff(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

}