package com.tashila.mywalletfree;

public class ExampleItem {
    private String mAmount;
    private String mDescr;
    private String mDate;

    public ExampleItem(String amount, String descr, String date) {
        mAmount = amount;
        mDescr = descr;
        mDate = date;
    }

    public String getAmount() {
        return mAmount;
    }

    public String getDescr() {
        return mDescr;
    }

    public String getDate() {
        return mDate;
    }
}
