package com.tashila.mywalletfree.investments;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;


import java.util.List;

public class InvestmentsRepository {
    private InvestmentsDao investmentsDao;
    private LiveData<List<Investment>> allInvestments;
    private LiveData<List<Investment>> investmentsSortByInvestValue;
    private LiveData<List<Investment>> investmentsSortByReturnValue;
    private LiveData<List<Investment>> investmentsSortByTime;
    private LiveData<List<Investment>> investmentsSortByMostProfitable;

    public InvestmentsRepository(Application application) {
        InvestmentsDatabase investmentsDatabase = InvestmentsDatabase.getInstance(application);
        investmentsDao = investmentsDatabase.investmentsDao();
        allInvestments = investmentsDao.getAllInvestments();
        investmentsSortByInvestValue = investmentsDao.getInvestmentsSortByInvestValue();
        investmentsSortByReturnValue = investmentsDao.getInvestmentsSortByReturnValue();
        investmentsSortByTime = investmentsDao.getInvestmentsSortByTime();
        investmentsSortByMostProfitable = investmentsDao.getInvestmentsSortByMostProfitable();
    }

    public void insert(Investment investment) {
        new InsertInvestmentAsyncTask(investmentsDao).execute(investment);
    }

    public void update(Investment investment) {
        new UpdateInvestmentAsyncTask(investmentsDao).execute(investment);
    }

    public void delete(Investment investment) {
        new DeleteInvestmentAsyncTask(investmentsDao).execute(investment);
    }

    public LiveData<List<Investment>> getAllInvestments() {
        return allInvestments;
    }

    public LiveData<List<Investment>> getInvestmentsSortByInvestValue() {
        return investmentsSortByInvestValue;
    }

    public LiveData<List<Investment>> getInvestmentsSortByReturnValue() {
        return investmentsSortByReturnValue;
    }

    public LiveData<List<Investment>> getInvestmentsSortByMostProfitable() {
        return investmentsSortByMostProfitable;
    }

    public LiveData<List<Investment>> getInvestmentsSortByTime() {
        return investmentsSortByTime;
    }

    private static class InsertInvestmentAsyncTask extends AsyncTask<Investment, Void, Void> {
        private InvestmentsDao investmentsDao;

        private InsertInvestmentAsyncTask(InvestmentsDao investmentsDao) {
            this.investmentsDao = investmentsDao;
        }

        @Override
        protected Void doInBackground(Investment... investments) {
            investmentsDao.insert(investments[0]);
            return null;
        }
    }

    private static class UpdateInvestmentAsyncTask extends AsyncTask<Investment, Void, Void> {
        private InvestmentsDao investmentsDao;

        private UpdateInvestmentAsyncTask(InvestmentsDao investmentsDao) {
            this.investmentsDao = investmentsDao;
        }

        @Override
        protected Void doInBackground(Investment... investments) {
            investmentsDao.update(investments[0]);
            return null;
        }
    }

    private static class DeleteInvestmentAsyncTask extends AsyncTask<Investment, Void, Void> {
        private InvestmentsDao investmentsDao;

        private DeleteInvestmentAsyncTask(InvestmentsDao investmentsDao) {
            this.investmentsDao = investmentsDao;
        }

        @Override
        protected Void doInBackground(Investment... investments) {
            investmentsDao.delete(investments[0]);
            return null;
        }
    }
}
