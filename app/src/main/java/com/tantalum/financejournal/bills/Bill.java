package com.tantalum.financejournal.bills;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bills_table")
public class Bill {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private boolean isPaid;
    private String title;
    private String amount;
    private String paidDate;
    private String dueDate;
    private String remarks;
    private boolean isMonthly;
    private int lastPaidMonth;

    public Bill(boolean isPaid, String title, String amount, String paidDate, String dueDate, String remarks, boolean isMonthly, int lastPaidMonth) {
        this.isPaid = isPaid;
        this.title = title;
        this.amount = amount;
        this.lastPaidMonth = lastPaidMonth;
        if (paidDate == null || paidDate.isEmpty())
            this.paidDate = "N/A";
        else
            this.paidDate = paidDate;
        if (dueDate == null || dueDate.isEmpty())
            this.dueDate = "N/A";
        else
            this.dueDate = dueDate;
        this.remarks = remarks;
        this.isMonthly = isMonthly;
    }

    public int getId() {
        return id;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public String getTitle() {
        return title;
    }

    public String getAmount() {
        return amount;
    }

    public String getPaidDate() {
        return paidDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getRemarks() {
        return remarks;
    }

    public boolean isMonthly() {
        return isMonthly;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPaid(boolean isPaid) {
        this.isPaid = isPaid;
    }

    public void setPaidDate(String paidDate) {
        this.paidDate = paidDate;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setMonthly(boolean monthly) {
        isMonthly = monthly;
    }

    public int getLastPaidMonth() {
        return lastPaidMonth;
    }

    public void setLastPaidMonth(int lastPaidMonth) {
        this.lastPaidMonth = lastPaidMonth;
    }
}
