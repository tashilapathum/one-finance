package com.tashila.mywalletfree.investments;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "investments_table")
public class Investment {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private double investValue;
    private double returnValue;
    private int dayCount;
    private long dateInMillis;
    private String tag;
    private List<String> history;

    public Investment(String title, String description, double investValue, double returnValue, int dayCount, long dateInMillis, String tag, List<String> history) {
        this.title = title;
        this.description = description;
        this.investValue = investValue;
        this.returnValue = returnValue;
        this.dayCount = dayCount;
        this.dateInMillis = dateInMillis;
        this.tag = tag;
        this.history = history;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getInvestValue() {
        return investValue;
    }

    public void setInvestValue(double investValue) {
        this.investValue = investValue;
    }

    public double getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(double returnValue) {
        this.returnValue = returnValue;
    }

    public int getDayCount() {
        return dayCount;
    }

    public void setDayCount(int dayCount) {
        this.dayCount = dayCount;
    }

    public long getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(long dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history;
    }
}
