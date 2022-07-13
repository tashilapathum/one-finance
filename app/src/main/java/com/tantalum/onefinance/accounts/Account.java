package com.tantalum.onefinance.accounts;

import java.io.Serializable;
import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts_table")
public class Account implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String accName;
    private String accBalance;
    private List<String> balanceHistory;
    private String moreDetails;
    private List<String> activities;

    public Account(String accName, String accBalance, List<String> balanceHistory, String moreDetails, List<String> activities) {
        this.accName = accName;
        this.accBalance = accBalance;
        this.balanceHistory = balanceHistory;
        this.moreDetails = moreDetails;
        this.activities = activities;
    }

    public int getId() {
        return id;
    }

    public String getAccName() {
        return accName;
    }

    public String getAccBalance() {
        return accBalance;
    }
    public String getMoreDetails() {
        return moreDetails;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getActivities() {
        return activities;
    }

    public List<String> getBalanceHistory() {
        return balanceHistory;
    }

    public void setAccName(String accName) {
        this.accName = accName;
    }

    public void setAccBalance(String accBalance) {
        this.accBalance = accBalance;
    }

    public void setBalanceHistory(List<String> balanceHistory) {
        this.balanceHistory = balanceHistory;
    }

    public void setMoreDetails(String moreDetails) {
        this.moreDetails = moreDetails;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }
}
