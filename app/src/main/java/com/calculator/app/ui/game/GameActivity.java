package com.calculator.app.ui.game;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class GameActivity extends Activity {

    public static final String EXTRA_GAME = "game_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int gameId = getIntent().getIntExtra(EXTRA_GAME, -1);

        GameSurfaceView view;
        switch (gameId) {
            case 99:   view = new GameSurfaceView(this, GameSurfaceView.GAME_PLATFORMER);   break;
            case 111:  view = new GameSurfaceView(this, GameSurfaceView.GAME_TETRIS);       break;
            default:   finish(); return;
        }

        setContentView(view);
    }
}
