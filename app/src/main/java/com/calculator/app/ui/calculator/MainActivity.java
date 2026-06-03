package com.calculator.app.ui.calculator;
import com.calculator.app.R;
import com.calculator.app.core.CalculatorEngine;
import com.calculator.app.core.GlobalScore;
import com.calculator.app.network.LoadDataActivity;
import com.calculator.app.ui.account.LoginActivity;
import com.calculator.app.ui.game.GameMenuActivity;
import com.calculator.app.ui.game.MiniGames;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView tvDisplay, tvMemory, tvExpression, tvGameMode;
    private CalculatorEngine engine;
    private boolean gameModeEnabled = false;
    private SharedPreferences prefs;

    @Override
    protected void onResume() {
        super.onResume();
        updateCoinDisplay();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefs  = getSharedPreferences("CalcPrefs", Context.MODE_PRIVATE);
        engine = new CalculatorEngine();

        tvDisplay    = findViewById(R.id.tvDisplay);
        tvMemory     = findViewById(R.id.tvMemory);
        tvExpression = findViewById(R.id.tvExpression);
        tvGameMode   = findViewById(R.id.tvGameMode);

        engine.inputOperation("Clear");
        updateDisplay();

        bindButtons();
        bindTopBar();
    }

    

    private void bindTopBar() {
        ImageButton btnMenu  = findViewById(R.id.btnMenu);
        ImageButton btnGame  = findViewById(R.id.btnGame);
        ImageButton btnLogin = findViewById(R.id.btnLogin);
        ImageButton btnLoad  = findViewById(R.id.btnLoad);

        if (btnMenu  != null) btnMenu.setOnClickListener(v ->
                Toast.makeText(this, "Меню", Toast.LENGTH_SHORT).show());

        if (btnGame  != null) btnGame.setOnClickListener(v -> openGameMenu());

        if (btnLogin != null) btnLogin.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));

        if (btnLoad  != null) btnLoad.setOnClickListener(v ->
                startActivity(new Intent(this, LoadDataActivity.class)));
    }

    

    private void bindButtons() {
        int[]    digitIds = {R.id.btn0,R.id.btn1,R.id.btn2,R.id.btn3,
                             R.id.btn4,R.id.btn5,R.id.btn6,R.id.btn7,
                             R.id.btn8,R.id.btn9};
        String[] digits   = {"0","1","2","3","4","5","6","7","8","9"};
        for (int i = 0; i < digitIds.length; i++) {
            final String d = digits[i];
            Button b = findViewById(digitIds[i]);
            if (b != null) b.setOnClickListener(v -> { engine.inputNum(d); updateDisplay(); });
        }

        find(R.id.btnDot).setOnClickListener(v -> { engine.inputOperation("."); updateDisplay(); });

        find(R.id.btnPlus).setOnClickListener(v     -> { engine.inputOperation("+"); updateDisplay(); });
        find(R.id.btnMinus).setOnClickListener(v    -> { engine.inputOperation("-"); updateDisplay(); });
        find(R.id.btnMultiply).setOnClickListener(v -> { engine.inputOperation("*"); updateDisplay(); });
        find(R.id.btnDivide).setOnClickListener(v   -> { engine.inputOperation("/"); updateDisplay(); });

        find(R.id.btnEqual).setOnClickListener(v -> doCalculate());

        find(R.id.btnDel).setOnClickListener(v       -> { engine.inputOperation("del");     updateDisplay(); });
        find(R.id.btnCE).setOnClickListener(v        -> { engine.inputOperation("ClrEntr"); updateDisplay(); });
        find(R.id.btnC).setOnClickListener(v         -> { engine.inputOperation("Clear");   updateDisplay(); tvExpression.setText(""); });
        find(R.id.btnPlusMinus).setOnClickListener(v -> { engine.inputOperation("PlsMns");  updateDisplay(); });
        find(R.id.btnRoot).setOnClickListener(v      -> { engine.inputOperation("Root");    updateDisplay(); });
        find(R.id.btnPercent).setOnClickListener(v   -> { engine.inputOperation("Perc");    updateDisplay(); });
        find(R.id.btnInverse).setOnClickListener(v   -> { engine.inputOperation("1/inp");   updateDisplay(); });

        find(R.id.btnMC).setOnClickListener(v     -> { engine.memoryClear();  tvMemory.setText(engine.getMemoryDisplay()); });
        find(R.id.btnMR).setOnClickListener(v     -> { engine.memoryRead();   updateDisplay(); });
        find(R.id.btnMS).setOnClickListener(v     -> { engine.memoryStore();  tvMemory.setText(engine.getMemoryDisplay()); });
        find(R.id.btnMPlus).setOnClickListener(v  -> { engine.memoryPlus();   tvMemory.setText(engine.getMemoryDisplay()); Toast.makeText(this,"M = "+engine.getMemory(),Toast.LENGTH_SHORT).show(); });
        find(R.id.btnMMinus).setOnClickListener(v -> { engine.memoryMinus();  tvMemory.setText(engine.getMemoryDisplay()); Toast.makeText(this,"M = "+engine.getMemory(),Toast.LENGTH_SHORT).show(); });
    }

    private Button find(int id) { return findViewById(id); }

    

    private void doCalculate() {
        String beforeExpr = engine.getCount();
        String result = engine.calculate();
        if (result == null) {
            tvDisplay.setText("Ошибка");
            tvExpression.setText("");
        } else {
            tvExpression.setText(beforeExpr + " =");
            tvDisplay.setText(result);
            autoScaleText(result);
            prefs.edit().putString("last_result", result).apply();
            if (gameModeEnabled) {
                try { MiniGames.checkAndLaunch(this, Double.parseDouble(result)); }
                catch (NumberFormatException ignored) {}
            }
        }
    }

    

    private void updateDisplay() {
        String text = engine.getCount();
        tvDisplay.setText(text);
        autoScaleText(text);
    }

    private void autoScaleText(String text) {
        int len = text.length();
        if      (len > 14) tvDisplay.setTextSize(22);
        else if (len > 11) tvDisplay.setTextSize(30);
        else if (len > 8)  tvDisplay.setTextSize(38);
        else if (len > 5)  tvDisplay.setTextSize(46);
        else               tvDisplay.setTextSize(52);
    }

    

    private void updateCoinDisplay() {
        tvGameMode.setVisibility(android.view.View.VISIBLE);
        tvGameMode.setText("🎮 Игры  |  💰 " + GlobalScore.get() + "  |  ⭐ XP: " + GlobalScore.getXp());
    }

    

    private void openGameMenu() {
        gameModeEnabled = true;
        updateCoinDisplay();
        startActivity(new Intent(this, GameMenuActivity.class));
    }
}
