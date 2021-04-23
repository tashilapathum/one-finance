package com.tashila.mywalletfree.accounts;

import android.app.Application;
import android.os.AsyncTask;

import java.util.List;
import java.util.concurrent.ExecutionException;

import androidx.lifecycle.LiveData;

public class AccountsRepository {

    private AccountsDao accountsDao;
    private List<Account> allAccounts;
    private LiveData<List<Account>> allAccountsLive;

    public AccountsRepository(Application application) {
        AccountsDatabase database = AccountsDatabase.getInstance(application);
        accountsDao = database.accountsDao();
    }

    public void insert(Account account) {
        new InsertAccountAsyncTask(accountsDao).execute(account);
    }

    public void update(Account account) {
        new UpdateAccountAsyncTask(accountsDao).execute(account);
    }

    public void delete(Account account) {
        new DeleteAccountAsyncTask(accountsDao).execute(account);
    }

    public List<Account> getAllAccounts() {
        try {
            return new GetAccountsListAccountAsyncTask(accountsDao).execute().get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public LiveData<List<Account>> getAllAccountsLive() {
        return accountsDao.getAllAccountsLive();
    }

    private static class InsertAccountAsyncTask extends AsyncTask<Account, Void, Void> {
        private AccountsDao accountsDao;

        private InsertAccountAsyncTask(AccountsDao accountsDao) {
            this.accountsDao = accountsDao;
        }

        @Override
        protected Void doInBackground(Account... accounts) {
            accountsDao.insert(accounts[0]);
            return null;
        }
    }

    private static class UpdateAccountAsyncTask extends AsyncTask<Account, Void, Void> {
        private AccountsDao accountsDao;

        private UpdateAccountAsyncTask(AccountsDao accountsDao) {
            this.accountsDao = accountsDao;
        }

        @Override
        protected Void doInBackground(Account... accounts) {
            accountsDao.update(accounts[0]);
            return null;
        }
    }

    private static class DeleteAccountAsyncTask extends AsyncTask<Account, Void, Void> {
        private AccountsDao accountsDao;

        private DeleteAccountAsyncTask(AccountsDao accountsDao) {
            this.accountsDao = accountsDao;
        }

        @Override
        protected Void doInBackground(Account... accounts) {
            accountsDao.delete(accounts[0]);
            return null;
        }
    }

    private static class GetAccountsListAccountAsyncTask extends AsyncTask<Account, Void, List<Account>> {
        private AccountsDao accountsDao;

        private GetAccountsListAccountAsyncTask(AccountsDao accountsDao) {
            this.accountsDao = accountsDao;
        }

        @Override
        protected List<Account> doInBackground(Account... accounts) {
            return accountsDao.getAllAccounts();
        }
    }

}
