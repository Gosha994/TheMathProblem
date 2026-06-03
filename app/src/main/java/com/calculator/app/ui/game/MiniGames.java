package com.calculator.app.ui.game;
import com.calculator.app.core.GlobalScore;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.widget.*;
import java.util.Random;

public class MiniGames {

    
    
    
    public static void checkAndLaunch(Context ctx, double result) {
        if (result != Math.floor(result)) return;
        long r = (long) result;
        if      (r == 777) launchSlotMachine(ctx);
        else if (r == 99)  launchGameActivity(ctx, 99);
        else if (r == 111) launchGameActivity(ctx, 111);
    }

    private static void launchGameActivity(Context ctx, int gameId) {
        Intent intent = new Intent(ctx, GameActivity.class);
        intent.putExtra(GameActivity.EXTRA_GAME, gameId);
        ctx.startActivity(intent);
    }

    
    
    
    static final int SLOT_FEE = 100;

    public static void launchSlotMachine(Context ctx) {

        int balance = GlobalScore.get();

        if (balance < SLOT_FEE) {
            new AlertDialog.Builder(ctx)
                .setTitle("🎰 Слот-машина")
                .setMessage("Нужно " + SLOT_FEE + " 💰 для входа!\n\nСейчас у тебя: " + balance + " 💰\n\nЗарабатывай монеты в играх:\n🏃 99 — Платформер\n🧩 111 — Тетрис")
                .setPositiveButton("OK", null)
                .show();
            return;
        }

        
        GlobalScore.add(-SLOT_FEE);
        final int pot = SLOT_FEE; 

        Random rnd = new Random();
        String[] ops = {"+", "-", "×", "÷"};

        
        LinearLayout root = makeRoot(ctx);
        root.setBackgroundColor(Color.parseColor("#1a1a2e"));

        TextView titleTv = makeText(ctx, "🎰 СЛОТ-МАШИНА", 22, true);
        titleTv.setTextColor(Color.parseColor("#FFD700"));
        titleTv.setGravity(Gravity.CENTER);

        TextView potTv = makeText(ctx, "Взнос: " + SLOT_FEE + " 💰  |  Баланс: " + GlobalScore.get() + " 💰", 15, false);
        potTv.setGravity(Gravity.CENTER);
        potTv.setTextColor(Color.parseColor("#AAAAFF"));

        
        LinearLayout wheelRow = new LinearLayout(ctx);
        wheelRow.setOrientation(LinearLayout.HORIZONTAL);
        wheelRow.setGravity(Gravity.CENTER);
        wheelRow.setPadding(0, 20, 0, 20);

        TextView[] wheels = new TextView[3];
        for (int i = 0; i < 3; i++) {
            TextView w = new TextView(ctx);
            w.setText(i == 1 ? "?" : "??");
            w.setTextSize(i == 1 ? 32 : 36);
            w.setTextColor(Color.WHITE);
            w.setGravity(Gravity.CENTER);
            w.setBackgroundColor(Color.parseColor("#16213e"));
            w.setPadding(i == 1 ? 14 : 20, 18, i == 1 ? 14 : 20, 18);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, i == 1 ? 0.65f : 1.17f);
            lp.setMargins(5, 0, 5, 0);
            w.setLayoutParams(lp);
            wheelRow.addView(w);
            wheels[i] = w;
        }

        TextView resultTv = makeText(ctx, "Нажми КРУТИТЬ!", 17, false);
        resultTv.setGravity(Gravity.CENTER);

        
        LinearLayout mathArea = new LinearLayout(ctx);
        mathArea.setOrientation(LinearLayout.VERTICAL);
        mathArea.setVisibility(android.view.View.GONE);

        TextView mathQuestion = makeText(ctx, "", 30, true);
        mathQuestion.setGravity(Gravity.CENTER);
        mathQuestion.setTextColor(Color.parseColor("#FFD700"));

        TextView mathHint = makeText(ctx, "", 14, false);
        mathHint.setGravity(Gravity.CENTER);
        mathHint.setTextColor(Color.parseColor("#AAAAFF"));

        EditText mathInput = new EditText(ctx);
        mathInput.setHint("Введи ответ...");
        mathInput.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
        mathInput.setTextColor(Color.WHITE);
        mathInput.setHintTextColor(Color.GRAY);
        mathInput.setGravity(Gravity.CENTER);
        mathInput.setTextSize(28);

        Button mathBtn = makeButton(ctx, "✅ Ответить");
        mathArea.addView(mathQuestion);
        mathArea.addView(mathHint);
        mathArea.addView(mathInput);
        mathArea.addView(mathBtn);

        Button spinBtn = makeButton(ctx, "🎰 КРУТИТЬ");

        root.addView(titleTv);
        root.addView(potTv);
        root.addView(wheelRow);
        root.addView(resultTv);
        root.addView(mathArea);
        root.addView(spinBtn);

        AlertDialog[] dlgHolder = {null};
        dlgHolder[0] = new AlertDialog.Builder(ctx)
                .setView(root)
                .setCancelable(false)
                .setNegativeButton("Закрыть", null)
                .show();

        Handler handler = new Handler(Looper.getMainLooper());

        spinBtn.setOnClickListener(v -> {
            spinBtn.setEnabled(false);
            mathArea.setVisibility(android.view.View.GONE);
            resultTv.setTextColor(Color.WHITE);
            resultTv.setText("🎰 Крутим...");
            for (TextView w2 : wheels) w2.setTextColor(Color.WHITE);

            
            int opIdx = rnd.nextInt(4);
            final int fA, fB, fCorrect;
            switch (opIdx) {
                case 0: { 
                    fA = rnd.nextInt(99) + 1;
                    fB = rnd.nextInt(99) + 1;
                    fCorrect = fA + fB;
                    break;
                }
                case 1: { 
                    int a = rnd.nextInt(99) + 1;
                    int b = rnd.nextInt(a) + 1;
                    fA = a; fB = b; fCorrect = a - b;
                    break;
                }
                case 2: { 
                    fA = rnd.nextInt(11) + 2;
                    fB = rnd.nextInt(11) + 2;
                    fCorrect = fA * fB;
                    break;
                }
                default: { 
                    int b = rnd.nextInt(11) + 2;
                    int mult = rnd.nextInt(8) + 2;
                    fA = b * mult; fB = b; fCorrect = mult;
                    break;
                }
            }

            final String fOp = ops[opIdx];
            final String fDisplayA = String.valueOf(fA);
            final String fDisplayB = String.valueOf(fB);
            final int multiplier = (opIdx >= 2) ? 4 : 2;
            final String mulLabel = (opIdx >= 2) ? "×4 (умн/дел)" : "×2 (сложение/вычитание)";
            final int winAmount = pot * multiplier;

            
            long[] stopDelays = {800, 1400, 2000};
            for (int i = 0; i < 3; i++) {
                final int idx = i;
                final long stopAt = stopDelays[i];
                new Runnable() {
                    final long startedAt = System.currentTimeMillis();
                    public void run() {
                        if (System.currentTimeMillis() - startedAt < stopAt) {
                            
                            if (idx == 1) {
                                wheels[1].setText(ops[rnd.nextInt(4)]);
                            } else {
                                
                                wheels[idx].setText(String.valueOf(rnd.nextInt(99) + 1));
                            }
                            handler.postDelayed(this, 75);
                        } else {
                            
                            if (idx == 0)      wheels[0].setText(fDisplayA);
                            else if (idx == 1) wheels[1].setText(fOp);
                            else               wheels[2].setText(fDisplayB);
                            wheels[idx].setTextColor(Color.parseColor("#FFD700"));

                            
                            if (idx == 2) {
                                handler.postDelayed(() -> {
                                    resultTv.setText("Реши пример и получи +1 XP! 🌟");
                                    resultTv.setTextColor(Color.parseColor("#FFD700"));
                                    mathQuestion.setText(fDisplayA + " " + fOp + " " + fDisplayB + " = ?");
                                    mathHint.setText("");
                                    mathInput.getText().clear();
                                    mathInput.setEnabled(true);
                                    mathBtn.setEnabled(true);
                                    mathArea.setVisibility(android.view.View.VISIBLE);
                                }, 500);
                            }
                        }
                    }
                }.run();
            }

            
            mathBtn.setOnClickListener(vv -> {
                String inputStr = mathInput.getText().toString().trim();
                if (inputStr.isEmpty()) return;
                try {
                    int userAnswer = Integer.parseInt(inputStr);
                    mathBtn.setEnabled(false);
                    mathInput.setEnabled(false);

                    if (userAnswer == fCorrect) {
                        GlobalScore.addXp(1);
                        resultTv.setText("✅ Верно! +1 XP (опыт: " + GlobalScore.getXp() + ")");
                        resultTv.setTextColor(Color.parseColor("#00CC44"));
                        mathHint.setText("");
                    } else {
                        resultTv.setText("❌ Неверно! Ответ: " + fCorrect);
                        resultTv.setTextColor(Color.RED);
                        mathHint.setText("");
                    }
                    
                    mathArea.setVisibility(android.view.View.GONE);
                    spinBtn.setText("🎰 КРУТИТЬ");
                    spinBtn.setEnabled(true);
                    
                    
                    
                    
                    spinBtn.setOnClickListener(v2 -> {
                        int newBalance = GlobalScore.get();
                        if (newBalance < SLOT_FEE) {
                            resultTv.setText("💸 Нужно " + SLOT_FEE + " 💰 для игры!");
                            resultTv.setTextColor(Color.RED);
                            return;
                        }
                        GlobalScore.add(-SLOT_FEE);
                        potTv.setText("Взнос: " + SLOT_FEE + " 💰  |  Баланс: " + GlobalScore.get() + " 💰");
                        spinBtn.setEnabled(false);
                        mathArea.setVisibility(android.view.View.GONE);
                        resultTv.setTextColor(Color.WHITE);
                        resultTv.setText("🎰 Крутим...");
                        for (TextView w2 : wheels) w2.setTextColor(Color.WHITE);

                        int opIdx2 = rnd.nextInt(4);
                        final int gA, gB, gCorrect;
                        switch (opIdx2) {
                            case 0: { gA = rnd.nextInt(99)+1; gB = rnd.nextInt(99)+1; gCorrect = gA+gB; break; }
                            case 1: { int a=rnd.nextInt(99)+1,b=rnd.nextInt(a)+1; gA=a;gB=b;gCorrect=a-b; break; }
                            case 2: { gA=rnd.nextInt(11)+2; gB=rnd.nextInt(11)+2; gCorrect=gA*gB; break; }
                            default:{ int b=rnd.nextInt(11)+2,m=rnd.nextInt(8)+2; gA=b*m;gB=b;gCorrect=m; break; }
                        }
                        final String gOp = ops[opIdx2];

                        long[] sd = {800,1400,2000};
                        for (int i2 = 0; i2 < 3; i2++) {
                            final int idx2 = i2;
                            new Runnable() {
                                final long t0 = System.currentTimeMillis();
                                public void run() {
                                    if (System.currentTimeMillis()-t0 < sd[idx2]) {
                                        wheels[idx2].setText(idx2==1 ? ops[rnd.nextInt(4)] : String.valueOf(rnd.nextInt(99)+1));
                                        handler.postDelayed(this, 75);
                                    } else {
                                        if (idx2==0) wheels[0].setText(String.valueOf(gA));
                                        else if (idx2==1) wheels[1].setText(gOp);
                                        else wheels[2].setText(String.valueOf(gB));
                                        wheels[idx2].setTextColor(Color.parseColor("#FFD700"));
                                        if (idx2==2) {
                                            handler.postDelayed(() -> {
                                                resultTv.setText("Реши пример и получи +1 XP! 🌟");
                                                resultTv.setTextColor(Color.parseColor("#FFD700"));
                                                mathQuestion.setText(gA + " " + gOp + " " + gB + " = ?");
                                                mathHint.setText("");
                                                mathInput.getText().clear();
                                                mathInput.setEnabled(true);
                                                mathBtn.setEnabled(true);
                                                mathArea.setVisibility(android.view.View.VISIBLE);
                                            }, 500);
                                        }
                                    }
                                }
                            }.run();
                        }
                        mathBtn.setOnClickListener(vv2 -> {
                            String s = mathInput.getText().toString().trim();
                            if (s.isEmpty()) return;
                            try {
                                int ans = Integer.parseInt(s);
                                mathBtn.setEnabled(false);
                                mathInput.setEnabled(false);
                                if (ans == gCorrect) {
                                    GlobalScore.addXp(1);
                                    resultTv.setText("✅ Верно! +1 XP (опыт: " + GlobalScore.getXp() + ")");
                                    resultTv.setTextColor(Color.parseColor("#00CC44"));
                                } else {
                                    resultTv.setText("❌ Неверно! Ответ: " + gCorrect);
                                    resultTv.setTextColor(Color.RED);
                                }
                                mathHint.setText("");
                                mathArea.setVisibility(android.view.View.GONE);
                                spinBtn.setText("🎰 КРУТИТЬ");
                                spinBtn.setEnabled(true);
                            } catch (NumberFormatException ex) {
                                mathHint.setText("Введи целое число!");
                            }
                        });
                    });

                } catch (NumberFormatException e) {
                    mathHint.setText("Введи целое число!");
                }
            });
        });
    }

    
    
    
    static LinearLayout makeRoot(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(32, 32, 32, 24);
        l.setBackgroundColor(Color.parseColor("#1a1a1a"));
        return l;
    }

    static TextView makeText(Context ctx, String text, int sp, boolean bold) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(sp);
        if (bold) tv.setTypeface(null, Typeface.BOLD);
        tv.setPadding(0, 8, 0, 8);
        return tv;
    }

    static Button makeButton(Context ctx, String label) {
        Button btn = new Button(ctx);
        btn.setText(label);
        btn.setTextColor(Color.WHITE);
        btn.setBackgroundColor(Color.parseColor("#2a2a4a"));
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 16, 0, 0);
        btn.setLayoutParams(p);
        return btn;
    }
}
