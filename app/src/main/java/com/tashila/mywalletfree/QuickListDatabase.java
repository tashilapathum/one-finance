package com.tashila.mywalletfree;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {QuickItem.class}, version = 1, exportSchema = false)
public abstract class QuickListDatabase extends RoomDatabase {

    private static QuickListDatabase instance;

    public abstract QuickListDao quickListDao();

    public static synchronized QuickListDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), QuickListDatabase.class, "quick_item_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
