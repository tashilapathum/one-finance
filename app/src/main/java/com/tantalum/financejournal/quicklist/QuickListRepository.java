package com.tantalum.financejournal.quicklist;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.lifecycle.LiveData;

public class QuickListRepository {
    private QuickListDao quickListDao;
    private LiveData<List<QuickItem>> allQuickItemsLiveData;

    public QuickListRepository(Application application) {
        QuickListDatabase database = QuickListDatabase.getInstance(application);
        quickListDao = database.quickListDao();
        allQuickItemsLiveData = quickListDao.getQuickItemsLiveData();
    }

    public void insert(QuickItem cartItem) {
        new InsertQuickItemAsyncTask(quickListDao).execute(cartItem);
    }

    public void update(QuickItem cartItem) {
        new UpdateQuickItemAsyncTask(quickListDao).execute(cartItem);
    }

    public void delete(QuickItem cartItem) {
        new DeleteQuickItemAsyncTask(quickListDao).execute(cartItem);
    }

    public LiveData<List<QuickItem>> getQuickItemsLiveData() {
        return allQuickItemsLiveData;
    }

    public List<QuickItem> getQuickItemsList() {
        try {
            return new GetQuickItemsListAsyncTask(quickListDao).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static class InsertQuickItemAsyncTask extends AsyncTask<QuickItem, Void, Void> {
        private QuickListDao quickListDao;

        private InsertQuickItemAsyncTask(QuickListDao quickListDao) {
            this.quickListDao = quickListDao;
        }

        @Override
        protected Void doInBackground(QuickItem... quickItems) {
            quickListDao.insert(quickItems[0]);
            return null;
        }
    }

    private static class UpdateQuickItemAsyncTask extends AsyncTask<QuickItem, Void, Void> {
        private QuickListDao quickListDao;

        private UpdateQuickItemAsyncTask(QuickListDao quickListDao) {
            this.quickListDao = quickListDao;
        }

        @Override
        protected Void doInBackground(QuickItem... quickItems) {
            quickListDao.update(quickItems[0]);
            return null;
        }
    }

    private static class DeleteQuickItemAsyncTask extends AsyncTask<QuickItem, Void, Void> {
        private QuickListDao quickListDao;

        private DeleteQuickItemAsyncTask(QuickListDao quickListDao) {
            this.quickListDao = quickListDao;
        }

        @Override
        protected Void doInBackground(QuickItem... quickItems) {
            quickListDao.delete(quickItems[0]);
            return null;
        }
    }

    private static class GetQuickItemsListAsyncTask extends AsyncTask<Void, Void, List<QuickItem>> {
        private QuickListDao quickListDao;

        private GetQuickItemsListAsyncTask(QuickListDao quickListDao) {
            this.quickListDao = quickListDao;
        }

        @Override
        protected List<QuickItem> doInBackground(Void... voids) {
            return quickListDao.getQuickItemsList();
        }
    }
}
