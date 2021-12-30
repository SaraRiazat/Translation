package com.shariaty.translation;

import android.app.Application;

import androidx.room.Room;

import com.shariaty.translation.local.TranslateDatabase;

public class TranslateApp extends Application {
    private TranslateDatabase translateDatabase;

    @Override
    public void onCreate() {
        super.onCreate();
        translateDatabase = Room.databaseBuilder(getApplicationContext(), TranslateDatabase.class, "translate_database")
                .fallbackToDestructiveMigration()
                .build();
    }

    public TranslateDatabase getTranslateDatabase() {
        return translateDatabase;
    }
}
