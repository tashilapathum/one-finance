package com.tashila.mywalletfree.loans;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "loans_table")
public class Loan {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private boolean isLent;
    private boolean isBorrowed;
    private boolean isSettled;
    private String person;
    private String amount;
    private String settledDate;
    private String lentDate;
    private String dueDate;
    private String details;

    public Loan(boolean isLent, boolean isBorrowed, boolean isSettled, String person, String amount, String settledDate, String lentDate, String dueDate, String details) {
        this.isLent = isLent;
        this.isBorrowed = isBorrowed;
        this.isSettled = isSettled;
        this.person = person;
        this.amount = amount;
        this.lentDate = lentDate;

        if (lentDate == null || lentDate.isEmpty()) this.lentDate = "N/A";
        else this.lentDate = lentDate;

        if (dueDate == null || dueDate.isEmpty()) this.dueDate = "N/A";
        else this.dueDate = dueDate;

        if (settledDate == null || settledDate.isEmpty()) this.settledDate = "N/A";
        else this.settledDate = settledDate;

        this.details = details;
    }

    public int getId() {
        return id;
    }

    public boolean isLent() {
        return isLent;
    }

    public String getPerson() {
        return person;
    }

    public String getAmount() {
        return amount;
    }

    public String getSettledDate() {
        if (settledDate == null)
            return "N/A";
        return settledDate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public String getDetails() {
        return details;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setLent(boolean isPaid) {
        this.isLent = isPaid;
    }

    public void setSettledDate(String settledDate) {
        this.settledDate = settledDate;
    }

    public void setPerson(String person) {
        this.person = person;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public boolean isBorrowed() {
        return isBorrowed;
    }

    public void setBorrowed(boolean borrowed) {
        isBorrowed = borrowed;
    }

    public boolean isSettled() {
        return isSettled;
    }

    public void setSettled(boolean settled) {
        isSettled = settled;
    }

    public String getLentDate() {
        return lentDate;
    }

    public void setLentDate(String lentDate) {
        this.lentDate = lentDate;
    }
}
