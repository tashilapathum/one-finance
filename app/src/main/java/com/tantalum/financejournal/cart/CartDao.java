package com.tantalum.financejournal.cart;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface CartDao {

    @Insert
    void insert(CartItem cartItem);

    @Update
    void update(CartItem cartItem);

    @Delete
    void delete(CartItem cartItem);

    @Query("DELETE FROM cart_items_table")
    void deleteAllCartItems();

    @Query("SELECT * FROM cart_items_table ORDER BY isChecked ASC")
    LiveData<List<CartItem>> getAllCartItems();
}
