package com.tantalum.onefinance.transactions;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.lifecycle.LiveData;

public class TransactionsRepository {
    private TransactionsDao transactionsDao;
    private LiveData<List<TransactionItem>> allTransactionItems;

    public TransactionsRepository(Application application) {
        TransactionsDatabase database = TransactionsDatabase.getInstance(application);
        transactionsDao = database.transactionsDao();
        allTransactionItems = transactionsDao.getAllTransactionItems();
    }

    public void insert(TransactionItem transactionItem) {
        new InsertTransactionAsyncTask(transactionsDao).execute(transactionItem);
    }

    public void update(TransactionItem transactionItem) {
        new UpdateTransactionAsyncTask(transactionsDao).execute(transactionItem);
    }

    public void delete(TransactionItem transactionItem) {
        new DeleteTransactionAsyncTask(transactionsDao).execute(transactionItem);
    }


    public LiveData<List<TransactionItem>> getAllTransactionItems() {
        return allTransactionItems;
    }

    public List<TransactionItem> getTransactionsList() throws ExecutionException, InterruptedException {
        return new GetTransactionsListAsyncTask(transactionsDao).execute().get();
    }

    private static class InsertTransactionAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionsDao transactionsDao;

        private InsertTransactionAsyncTask(TransactionsDao transactionsDao) {
            this.transactionsDao = transactionsDao;
        }

        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionsDao.insert(transactionItems[0]);
            return null;
        }
    }

    private static class UpdateTransactionAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionsDao transactionsDao;

        private UpdateTransactionAsyncTask(TransactionsDao transactionsDao) {
            this.transactionsDao = transactionsDao;
        }

        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionsDao.update(transactionItems[0]);
            return null;
        }
    }

    private static class DeleteTransactionAsyncTask extends AsyncTask<TransactionItem, Void, Void> {
        private TransactionsDao transactionsDao;

        private DeleteTransactionAsyncTask(TransactionsDao transactionsDao) {
            this.transactionsDao = transactionsDao;
        }

        @Override
        protected Void doInBackground(TransactionItem... transactionItems) {
            transactionsDao.delete(transactionItems[0]);
            return null;
        }
    }

    private static class GetTransactionsListAsyncTask extends AsyncTask<Void, Void, List<TransactionItem>> {
        private TransactionsDao transactionsDao;

        private GetTransactionsListAsyncTask(TransactionsDao transactionsDao) {
            this.transactionsDao = transactionsDao;
        }

        @Override
        protected List<TransactionItem> doInBackground(Void... voids) {
            return transactionsDao.getTransactionsList();
        }
    }
}



















