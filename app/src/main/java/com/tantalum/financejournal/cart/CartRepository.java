package com.tantalum.financejournal.cart;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class CartRepository {
    private CartDao cartDao;
    private LiveData<List<CartItem>> allCartItems;

    public CartRepository(Application application) {
        CartDatabase database = CartDatabase.getInstance(application);
        cartDao = database.cartItemDao();
        allCartItems = cartDao.getAllCartItems();
    }

    public void insert(CartItem cartItem) {
        new InsertCartItemAsyncTask(cartDao).execute(cartItem);
    }

    public void update(CartItem cartItem) {
        new UpdateCartItemAsyncTask(cartDao).execute(cartItem);
    }

    public void delete(CartItem cartItem) {
        new DeleteCartItemAsyncTask(cartDao).execute(cartItem);
    }

    public void deleteAll() {
        new DeleteAllCartItemsAsyncTask(cartDao).execute();
    }

    public LiveData<List<CartItem>> getAllCartItems() {
        return allCartItems;
    }

    private static class InsertCartItemAsyncTask extends AsyncTask<CartItem, Void, Void> {
        private CartDao cartDao;

        private InsertCartItemAsyncTask(CartDao cartDao) {
            this.cartDao = cartDao;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            cartDao.insert(cartItems[0]);
            return null;
        }
    }

    private static class UpdateCartItemAsyncTask extends AsyncTask<CartItem, Void, Void> {
        private CartDao cartDao;

        private UpdateCartItemAsyncTask(CartDao cartDao) {
            this.cartDao = cartDao;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            cartDao.update(cartItems[0]);
            return null;
        }
    }

    private static class DeleteCartItemAsyncTask extends AsyncTask<CartItem, Void, Void> {
        private CartDao cartDao;

        private DeleteCartItemAsyncTask(CartDao cartDao) {
            this.cartDao = cartDao;
        }

        @Override
        protected Void doInBackground(CartItem... cartItems) {
            cartDao.delete(cartItems[0]);
            return null;
        }
    }

    private static class DeleteAllCartItemsAsyncTask extends AsyncTask<Void, Void, Void> {
        private CartDao cartDao;

        private DeleteAllCartItemsAsyncTask(CartDao cartDao) {
            this.cartDao = cartDao;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            cartDao.deleteAllCartItems();
            return null;
        }
    }
}
