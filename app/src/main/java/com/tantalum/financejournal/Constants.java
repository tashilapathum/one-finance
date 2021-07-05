package com.tantalum.financejournal;

public class Constants {
    //shared preferences
    public static final String SP_CATEGORIES = "categories";
    public static final String SP_BALANCE = "balance";

    //free limits
    public static final int FREE_INVESTMENTS_LIMIT = 3;
    public static final int FREE_BILLS_LIMIT = 10;
    public static final int FREE_LOANS_LIMIT = 10;
    public static final int FREE_QUICKLIST_LIMIT = 5;

    //transaction types
    public static final int EXPENSE = 1;
    public static final int INCOME = 2;
    public static final int TRANSFER = 3;
    public static final int DEPOSIT = 4;
    public static final int WITHDRAWAL = 5;
    public static final int PAYMENT = 6;

}

//TODO: make it only possible to add unique bank account names