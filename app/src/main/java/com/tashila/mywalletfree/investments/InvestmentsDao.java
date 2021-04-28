package com.tashila.mywalletfree.investments;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InvestmentsDao {

    @Insert
    void insert(Investment investment);

    @Update
    void update(Investment investment);

    @Delete
    void delete(Investment investment);

    @Query("SELECT * FROM investments_table")
    LiveData<List<Investment>> getAllInvestments();

    @Query("SELECT * FROM investments_table ORDER BY investValue")
    LiveData<List<Investment>> getInvestmentsSortByInvestValue();

    @Query("SELECT * FROM investments_table ORDER BY returnValue") //profit percentage is different from return value because
    LiveData<List<Investment>> getInvestmentsSortByReturnValue();  //the invested values could be relatively large or small

    @Query("SELECT * FROM investments_table ORDER BY (investValue - returnValue) / investValue * 100")
    LiveData<List<Investment>> getInvestmentsSortByMostProfitable(); //profit percentage
}
