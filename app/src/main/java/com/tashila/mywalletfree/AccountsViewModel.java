package com.tashila.mywalletfree;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class AccountsViewModel extends AndroidViewModel {
    private AccountsRepository repository;

    public AccountsViewModel(@NonNull Application application) {
        super(application);
        repository = new AccountsRepository(application);
    }

    public void insert(Account account) {
        repository.insert(account);
    }

    public void update(Account account) {
        repository.update(account);
    }

    public void delete(Account account) {
        repository.delete(account);
    }

    public List<Account> getAllAccounts() {
        return repository.getAllAccounts();
    }

    public LiveData<List<Account>> getAllAccountsLive() {
        return repository.getAllAccountsLive();
    }

}
