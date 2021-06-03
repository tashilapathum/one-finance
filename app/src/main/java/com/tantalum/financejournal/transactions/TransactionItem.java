package com.tantalum.financejournal.transactions;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions_table")
public class TransactionItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String balance;
    private String prefix;
    private String amount;
    private String description;
    private String userDate;
    private String databaseDate;
    private boolean isBankRelated;

    public TransactionItem(String balance, String prefix, String amount, String description, String userDate,
                           String databaseDate, boolean isBankRelated) {
        this.balance = balance;
        this.prefix = prefix;
        this.amount = amount;
        this.description = description;
        this.userDate = userDate;
        this.databaseDate = databaseDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUserDate(String userDate) {
        this.userDate = userDate;
    }

    public int getId() {
        return id;
    }

    public String getBalance() {
        return balance;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getAmount() {
        return amount;
    }

    public double getAmountValue() {
        return Double.parseDouble(amount);
    }

    public String getDescription() {
        return description;
    }

    public String getUserDate() {
        return userDate;
    }

    public String getDatabaseDate() {
        return databaseDate;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public void setDatabaseDate(String databaseDate) {
        this.databaseDate = databaseDate;
    }

    public boolean isBankRelated() {
        return isBankRelated;
    }

    public void setBankRelated(boolean bankRelated) {
        isBankRelated = bankRelated;
    }
}
