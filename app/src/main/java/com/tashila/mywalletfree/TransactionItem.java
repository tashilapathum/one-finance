package com.tashila.mywalletfree;

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
    private int dayOfWeek;
    private int dayOfMonth;
    private int monthOfYear;

    public TransactionItem(String balance, String prefix, String amount, String description, String userDate, //for user
                           String databaseDate, int dayOfWeek, int dayOfMonth, int monthOfYear) { //for database
        this.balance = balance;
        this.prefix = prefix;
        this.amount = amount;
        this.description = description;
        this.userDate = userDate;
        this.databaseDate = databaseDate;
        this.dayOfWeek = dayOfWeek;
        this.dayOfMonth = dayOfMonth;
        this.monthOfYear = monthOfYear;
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

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public void setMonthOfYear(int monthOfYear) {
        this.monthOfYear = monthOfYear;
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

    public String getDescription() {
        return description;
    }

    public String getUserDate() {
        return userDate;
    }

    public String getDatabaseDate() {
        return databaseDate;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getMonthOfYear() {
        return monthOfYear;
    }
}
