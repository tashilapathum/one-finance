package com.tashila.mywalletfree;

import android.app.Application;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class CartViewModel extends AndroidViewModel {
    private CartRepository repository;

    public CartViewModel(@NonNull Application application) {
        super(application);
        repository = new CartRepository(application);
    }

    public void insert(CartItem cartItem) {
        repository.insert(cartItem);
    }

    public void update(CartItem cartItem) {
        repository.update(cartItem);
    }

    public void delete(CartItem cartItem) {
        repository.delete(cartItem);
    }

    public void deleteAllCartItems() {
        repository.deleteAll();
    }

    public LiveData<List<CartItem>> getAllCartItems() {
        return repository.getAllCartItems();
    }
}
