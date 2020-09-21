package com.tashila.mywalletfree;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Account.class}, version = 2, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AccountsDatabase extends RoomDatabase {

    private static AccountsDatabase instance;

    public abstract AccountsDao accountsDao();

    public static synchronized AccountsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AccountsDatabase.class, "accounts_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
