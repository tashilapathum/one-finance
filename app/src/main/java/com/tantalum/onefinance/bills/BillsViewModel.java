package com.tantalum.onefinance.bills;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class BillsViewModel extends AndroidViewModel {
    private BillsRepository repository;

    public BillsViewModel(@NonNull Application application) {
        super(application);
        repository = new BillsRepository(application);
    }

    public void insert(Bill bill) {
        repository.insert(bill);
    }

    public void update(Bill bill) {
        repository.update(bill);
    }

    public void delete(Bill bill) {
        repository.delete(bill);
    }

    public LiveData<List<Bill>> getAllBills(boolean isPaid) {
        return repository.getAllBills(isPaid);
    }
}
