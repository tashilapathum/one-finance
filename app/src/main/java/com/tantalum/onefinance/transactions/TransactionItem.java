package com.tantalum.onefinance.transactions;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions_table")
public class TransactionItem {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String balance;
    private String prefix; //"+"income "-"expense ""transfer/deposit
    private String amount;
    private String description;
    private String timeInMillis;
    private String category; //only applicable for expenses. otherwise null

    public TransactionItem(String balance, String prefix, String amount, String description, String timeInMillis, String category) {
        this.balance = balance;
        this.prefix = prefix;
        this.amount = amount;
        this.description = description;
        this.timeInMillis = timeInMillis;
        this.category = category;
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

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(String timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
