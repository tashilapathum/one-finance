package com.tantalum.financejournal.quicklist;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface QuickListDao {

    @Insert
    void insert(QuickItem quickItem);

    @Update
    void update(QuickItem quickItem);

    @Delete
    void delete(QuickItem quickItem);

    @Query("SELECT * FROM quick_items_table")
    List<QuickItem> getQuickItemsList();

    @Query("SELECT * FROM quick_items_table")
    LiveData<List<QuickItem>> getQuickItemsLiveData();

}
