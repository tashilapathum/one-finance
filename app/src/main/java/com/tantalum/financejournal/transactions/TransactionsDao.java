package com.tantalum.financejournal.transactions;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TransactionsDao {

    @Insert
    void insert(TransactionItem transactionItem);

    @Update
    void update(TransactionItem transactionItem);

    @Delete
    void delete(TransactionItem transactionItem);

    @Query("SELECT * FROM transactions_table ORDER BY id DESC")
    LiveData<List<TransactionItem>> getAllTransactionItems();

    @Query("SELECT * FROM transactions_table ORDER BY id DESC")
    List<TransactionItem> getTransactionsList();

}
