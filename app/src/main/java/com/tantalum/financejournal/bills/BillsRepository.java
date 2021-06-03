package com.tantalum.financejournal.bills;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;

import androidx.lifecycle.LiveData;

public class BillsRepository {
    private BillsDao billsDao;
    private LiveData<List<Bill>> allPaidBills;
    private LiveData<List<Bill>> allDueBills;

    public BillsRepository(Application application) {
        BillsDatabase database = BillsDatabase.getInstance(application);
        billsDao = database.billsDao();
        allPaidBills = billsDao.getAllBills(true);
        allDueBills = billsDao.getAllBills(false);
    }

    public void insert(Bill bill) {
        new InsertBillsAsyncTask(billsDao).execute(bill);
    }

    public void update(Bill bill) {
        new UpdateBillsAsyncTask(billsDao).execute(bill);
    }

    public void delete(Bill bill) {
        new DeleteBillsAsyncTask(billsDao).execute(bill);
    }

    public LiveData<List<Bill>> getAllBills(boolean isPaid) {
        return billsDao.getAllBills(isPaid);
    }

    private static class InsertBillsAsyncTask extends AsyncTask<Bill, Void, Void> {
        private BillsDao billsDao;

        private InsertBillsAsyncTask(BillsDao billsDao) {
            this.billsDao = billsDao;
        }

        @Override
        protected Void doInBackground(Bill... bills) {
            billsDao.insert(bills[0]);
            return null;
        }
    }

    private static class UpdateBillsAsyncTask extends AsyncTask<Bill, Void, Void> {
        private BillsDao billsDao;

        private UpdateBillsAsyncTask(BillsDao billsDao) {
            this.billsDao = billsDao;
        }

        @Override
        protected Void doInBackground(Bill... bills) {
            billsDao.update(bills[0]);
            return null;
        }
    }

    private static class DeleteBillsAsyncTask extends AsyncTask<Bill, Void, Void> {
        private BillsDao billsDao;

        private DeleteBillsAsyncTask(BillsDao billsDao) {
            this.billsDao = billsDao;
        }

        @Override
        protected Void doInBackground(Bill... bills) {
            billsDao.delete(bills[0]);
            return null;
        }
    }

}
