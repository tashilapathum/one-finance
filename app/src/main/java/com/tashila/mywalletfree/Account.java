package com.tashila.mywalletfree;

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
    private String interestRate;
    private boolean isMultiInterest;
    private int interestLastCalcMonth;
    private String accNumber;
    private String moreDetails;
    private List<String> activities;
    private boolean isSelected;

    public Account(String accName, String accBalance, List<String> balanceHistory, String interestRate,
                   boolean isMultiInterest, int interestLastCalcMonth, String accNumber, String moreDetails, List<String> activities,
                   boolean isSelected) {
        this.accName = accName;
        this.accBalance = accBalance;
        this.balanceHistory = balanceHistory;
        this.interestRate = interestRate;
        this.isMultiInterest = isMultiInterest;
        this.interestLastCalcMonth = interestLastCalcMonth;
        this.accNumber = accNumber;
        this.moreDetails = moreDetails;
        this.activities = activities;
        this.isSelected = isSelected;
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

    public String getInterestRate() {
        return interestRate;
    }

    public boolean isMultiInterest() {
        return isMultiInterest;
    }

    public String getAccNumber() {
        return accNumber;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
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

    public void setInterestRate(String interestRate) {
        this.interestRate = interestRate;
    }

    public void setMultiInterest(boolean multiInterest) {
        isMultiInterest = multiInterest;
    }

    public void setAccNumber(String accNumber) {
        this.accNumber = accNumber;
    }

    public void setMoreDetails(String moreDetails) {
        this.moreDetails = moreDetails;
    }

    public void setActivities(List<String> activities) {
        this.activities = activities;
    }

    public int getInterestLastCalcMonth() {
        return interestLastCalcMonth;
    }

    public void setInterestLastCalcMonth(int interestLastCalcMonth) {
        this.interestLastCalcMonth = interestLastCalcMonth;
    }
}
