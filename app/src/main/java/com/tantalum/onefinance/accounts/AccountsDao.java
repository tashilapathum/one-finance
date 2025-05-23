package com.tantalum.onefinance.accounts;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface AccountsDao {

    @Insert
    void insert(Account account);

    @Update
    void update(Account account);

    @Delete
    void delete(Account account);

    @Query("SELECT * FROM accounts_table")
    List<Account> getAllAccounts();

    @Query("SELECT * FROM accounts_table")
    LiveData<List<Account>> getAllAccountsLive();

}
