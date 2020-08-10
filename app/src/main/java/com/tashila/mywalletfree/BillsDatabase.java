package com.tashila.mywalletfree;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Bill.class}, version = 2)
public abstract class BillsDatabase extends RoomDatabase {

    private static BillsDatabase instance;

    public abstract BillsDao billsDao();

    public static synchronized BillsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), BillsDatabase.class, "bills_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
