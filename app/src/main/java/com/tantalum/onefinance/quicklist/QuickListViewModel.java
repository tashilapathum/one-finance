package com.tantalum.onefinance.quicklist;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class QuickListViewModel extends AndroidViewModel {
    private QuickListRepository repository;

    public QuickListViewModel(@NonNull Application application) {
        super(application);
        repository = new QuickListRepository(application);
    }

    public void insert(QuickItem quickItem) {
        repository.insert(quickItem);
    }

    public void update(QuickItem quickItem) {
        repository.update(quickItem);
    }

    public void delete(QuickItem quickItem) {
        repository.delete(quickItem);
    }

    public LiveData<List<QuickItem>> getQuickItemsLiveData() {
        return repository.getQuickItemsLiveData();
    }

    public List<QuickItem> getQuickItemsList() {
        return repository.getQuickItemsList();
    }
}
