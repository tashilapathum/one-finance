package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.DecimalFormat;

public class Amount {
    private double value;
    private String valueString;
    private String currency;
    private SharedPreferences sharedPref;
    private Context context;
    private DecimalFormat df;


    public Amount(Context context, double value) {
        this.context = context;
        initialize();
        this.value = value;
    }

    public Amount(Context context, String valueString) {
        this.context = context;
        initialize();
        if (valueString != null)
            this.valueString = valueString.replace(currency, "");
    }

    private void initialize() {
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
        df = new DecimalFormat("#.00");
    }

    public String getAmountString() {
        if (value == 0)
            return "0.00";
        else
            return df.format(value);
    }

    public String getAmountWithCurrency() {
        if (value == 0)
            return currency + "0.00";
        else
            return currency + df.format(value);
    }

    public double getAmountValue() {
        if (valueString != null)
            return Double.parseDouble(valueString);
        else
            return 0;
    }
}
