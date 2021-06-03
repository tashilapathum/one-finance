package com.tantalum.financejournal.loans;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class LoansViewModel extends AndroidViewModel {
    private LoansRepository repository;

    public LoansViewModel(@NonNull Application application) {
        super(application);
        repository = new LoansRepository(application);
    }

    public void insert(Loan loan) {
        repository.insert(loan);
    }

    public void update(Loan loan) {
        repository.update(loan);
    }

    public void delete(Loan loan) {
        repository.delete(loan);
    }

    public LiveData<List<Loan>> getAllLoans(boolean isLent) {
        return repository.getAllLoans(isLent);
    }
}
