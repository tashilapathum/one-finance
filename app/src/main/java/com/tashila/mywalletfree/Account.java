package com.tashila.mywalletfree;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts_table")
public class Account {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String accName;
    private String accBalance;
    private String interestRate;
    private String multiInterests;
    private String accNumber;
    private String moreDetails;

    public Account(String accName, String accBalance, String interestRate, String multiInterests, String accNumber, String moreDetails) {
        this.accName = accName;
        this.accBalance = accBalance;
        this.interestRate = interestRate;
        this.multiInterests = multiInterests;
        this.accNumber = accNumber;
        this.moreDetails = moreDetails;
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

    public String getMultiInterests() {
        return multiInterests;
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
}
