package com.tantalum.financejournal.loans;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LoansDao {

    @Insert
    void insert(Loan loan);

    @Update
    void update(Loan loan);

    @Delete
    void delete(Loan loan);

    @Query("SELECT * FROM loans_table WHERE isLent = :isLent ORDER BY id AND isSettled ASC")
    LiveData<List<Loan>> getAllLoans(boolean isLent);

}
