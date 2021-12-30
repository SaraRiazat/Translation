package com.shariaty.translation.local;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TranslateModel.class}, version = 1)
public abstract class TranslateDatabase extends RoomDatabase {
    public abstract TranslateDao translateDao();
}
