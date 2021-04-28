package com.tashila.mywalletfree.investments;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Investment.class}, version = 1, exportSchema = false)
public abstract class InvestmentsDatabase extends RoomDatabase {

    private static InvestmentsDatabase instance;

    public abstract InvestmentsDao investmentsDao();

    public static synchronized InvestmentsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), InvestmentsDatabase.class, "investments_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
