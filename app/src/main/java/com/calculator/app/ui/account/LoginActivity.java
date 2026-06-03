package com.calculator.app.ui.account;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    
    public static final String BASE_URL = "http://10.0.2.2:5000";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView webView = new WebView(this);
        setContentView(webView);

        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                
                CookieManager.getInstance().flush();
                SharedPreferences prefs = getSharedPreferences("CalcPrefs", Context.MODE_PRIVATE);
                prefs.edit()
                     .putString("session_cookies",
                                CookieManager.getInstance().getCookie(BASE_URL))
                     .apply();
            }
        });

        webView.loadUrl(BASE_URL + "/login");
    }
}
