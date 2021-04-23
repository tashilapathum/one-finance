package com.tashila.mywalletfree;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Loan.class}, version = 1, exportSchema = false)
public abstract class LoansDatabase extends RoomDatabase {

    private static LoansDatabase instance;

    public abstract LoansDao loansDao();

    public static synchronized LoansDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), LoansDatabase.class, "loans_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
