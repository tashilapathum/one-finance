package com.tashila.mywalletfree;

import android.app.Application;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class TransactionsViewModel extends AndroidViewModel {
    private TransactionsRepository repository;

    public TransactionsViewModel(@NonNull Application application) {
        super(application);
        repository = new TransactionsRepository(application);
    }

    public void insert(TransactionItem transactionItem) {
        repository.insert(transactionItem);
    }

    public void update(TransactionItem transactionItem) {
        repository.update(transactionItem);
    }

    public void delete(TransactionItem transactionItem) {
        repository.delete(transactionItem);
    }

    public LiveData<List<TransactionItem>> getAllTransactionItems() {
       return repository.getAllTransactionItems();
    }

    public List<TransactionItem> getTransactionsList() throws ExecutionException, InterruptedException {
        return repository.getTransactionsList();
    }
}
