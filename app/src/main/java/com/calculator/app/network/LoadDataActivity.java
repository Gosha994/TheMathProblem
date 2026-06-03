package com.calculator.app.network;
import com.calculator.app.ui.account.LoginActivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoadDataActivity extends AppCompatActivity {

    private TextView tvResult;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setBackgroundColor(0xFF1a1a1a);

        tvResult = new TextView(this);
        tvResult.setTextColor(0xFFFFFFFF);
        tvResult.setTextSize(20);
        tvResult.setGravity(Gravity.CENTER);
        tvResult.setText("Загрузка данных…");
        root.addView(tvResult);

        setContentView(root);

        loadUserData();
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("CalcPrefs", Context.MODE_PRIVATE);
        String cookies = prefs.getString("session_cookies", "");

        executor.execute(() -> {
            String result = fetchData(cookies);
            mainHandler.post(() -> displayResult(result));
        });
    }

    private String fetchData(String cookies) {
        try {
            URL url = new URL(LoginActivity.BASE_URL + "/api/TMP/load");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            if (cookies != null && !cookies.isEmpty()) {
                conn.setRequestProperty("Cookie", cookies);
            }
            conn.setConnectTimeout(8000);
            conn.setReadTimeout(8000);

            int code = conn.getResponseCode();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            code >= 400 ? conn.getErrorStream() : conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();
            return sb.toString();
        } catch (Exception e) {
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    private void displayResult(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            if (obj.has("authenticated") && !obj.getBoolean("authenticated")) {
                tvResult.setText("❌ Вы не вошли в аккаунт.\nНажмите кнопку 👤 и войдите.");
                return;
            }
            if (obj.has("save_not_found")) {
                tvResult.setText("ℹ️ Сохранение не найдено на сервере.");
                return;
            }
            if (obj.has("error")) {
                tvResult.setText("⚠️ Ошибка:\n" + obj.getString("error"));
                return;
            }

            
            double balance = obj.optDouble("balance", Double.NaN);
            if (!Double.isNaN(balance)) {
                
                getSharedPreferences("CalcPrefs", Context.MODE_PRIVATE)
                        .edit().putFloat("balance", (float) balance).apply();
                tvResult.setText("✅ Данные загружены\n\n💰 Баланс: " + balance);
            } else {
                tvResult.setText("📦 Ответ сервера:\n" + json);
            }
        } catch (Exception e) {
            tvResult.setText("📦 Ответ сервера:\n" + json);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
