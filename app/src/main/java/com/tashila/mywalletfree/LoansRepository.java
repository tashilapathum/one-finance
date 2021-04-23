package com.tashila.mywalletfree;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import java.util.List;

public class LoansRepository {
    private LoansDao loansDao;
    private LiveData<List<Loan>> allPaidLoans;
    private LiveData<List<Loan>> allDueLoans;

    public LoansRepository(Application application) {
        LoansDatabase database = LoansDatabase.getInstance(application);
        loansDao = database.loansDao();
        allPaidLoans = loansDao.getAllLoans(true);
        allDueLoans = loansDao.getAllLoans(false);
    }

    public void insert(Loan loan) {
        new InsertLoansAsyncTask(loansDao).execute(loan);
    }

    public void update(Loan loan) {
        new UpdateLoansAsyncTask(loansDao).execute(loan);
    }

    public void delete(Loan loan) {
        new DeleteLoansAsyncTask(loansDao).execute(loan);
    }

    public LiveData<List<Loan>> getAllLoans(boolean isLent) {
        return loansDao.getAllLoans(isLent);
    }

    private static class InsertLoansAsyncTask extends AsyncTask<Loan, Void, Void> {
        private LoansDao loansDao;

        private InsertLoansAsyncTask(LoansDao loansDao) {
            this.loansDao = loansDao;
        }

        @Override
        protected Void doInBackground(Loan... loans) {
            loansDao.insert(loans[0]);
            return null;
        }
    }

    private static class UpdateLoansAsyncTask extends AsyncTask<Loan, Void, Void> {
        private LoansDao loansDao;

        private UpdateLoansAsyncTask(LoansDao loansDao) {
            this.loansDao = loansDao;
        }

        @Override
        protected Void doInBackground(Loan... loans) {
            loansDao.update(loans[0]);
            return null;
        }
    }

    private static class DeleteLoansAsyncTask extends AsyncTask<Loan, Void, Void> {
        private LoansDao loansDao;

        private DeleteLoansAsyncTask(LoansDao loansDao) {
            this.loansDao = loansDao;
        }

        @Override
        protected Void doInBackground(Loan... loans) {
            loansDao.delete(loans[0]);
            return null;
        }
    }

}
