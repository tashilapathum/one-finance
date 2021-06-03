package com.tantalum.financejournal.bills;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface BillsDao {

    @Insert
    void insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("SELECT * FROM bills_table WHERE isPaid = :isPaid ORDER BY id DESC")
    LiveData<List<Bill>> getAllBills(boolean isPaid);

}
