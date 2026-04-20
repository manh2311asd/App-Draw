package com.example.appdraw;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.appdraw.utils.FloatingChatbotManager;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        SharedPreferences prefs = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean("NightMode", false);
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        // Initialize the Floating Chatbot Manager
        FloatingChatbotManager.getInstance().init(this);
    }
}
