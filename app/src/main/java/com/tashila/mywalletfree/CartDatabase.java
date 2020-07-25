package com.tashila.mywalletfree;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {CartItem.class}, version = 2)
public abstract class CartDatabase extends RoomDatabase {

    private static CartDatabase instance;

    public abstract CartDao cartItemDao();

    public static synchronized CartDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    CartDatabase.class, "cart_item_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomCallback)
                    .build();
        }
        return instance;
    }

    private static RoomDatabase.Callback roomCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }
    };

    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void> {
        private CartDao cartDao;

        private PopulateDbAsyncTask(CartDatabase db) {
            cartDao = db.cartItemDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }

}
