package com.tashila.mywalletfree.cart;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {CartItem.class}, version = 5, exportSchema = false)
public abstract class CartDatabase extends RoomDatabase {

    private static CartDatabase instance;

    public abstract CartDao cartItemDao();

    public static synchronized CartDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), CartDatabase.class, "cart_item_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
