package com.tantalum.financejournal.transactions;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TransactionItem.class}, version = 2, exportSchema = false)
public abstract class TransactionsDatabase extends RoomDatabase {

    private static TransactionsDatabase instance;

    public abstract TransactionsDao transactionsDao();

    public static synchronized TransactionsDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    TransactionsDatabase.class, "transactions_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }

}
