package com.calculator.app.ui.game;
import com.calculator.app.R;
import com.calculator.app.core.GlobalScore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class GameMenuActivity extends AppCompatActivity {

    private TextView tvMenuStats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_menu);

        tvMenuStats = findViewById(R.id.tvMenuStats);
        updateStats();

        
        findViewById(R.id.btnSlotMachine).setOnClickListener(v -> {
            MiniGames.launchSlotMachine(this);
            updateStats();
        });

        
        findViewById(R.id.btnPlatformer).setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_GAME, 99);
            startActivity(intent);
        });

        
        findViewById(R.id.btnTetris).setOnClickListener(v -> {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra(GameActivity.EXTRA_GAME, 111);
            startActivity(intent);
        });

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
    }

    private void updateStats() {
        tvMenuStats.setText("💰 " + GlobalScore.get() + " монет   |   ⭐ XP: " + GlobalScore.getXp());
    }
}
