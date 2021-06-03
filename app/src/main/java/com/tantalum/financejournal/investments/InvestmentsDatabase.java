package com.tantalum.financejournal.investments;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.tantalum.financejournal.Converters;

@Database(entities = {Investment.class}, version = 3, exportSchema = false)
@TypeConverters(Converters.class)
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
