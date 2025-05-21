package com.tantalum.onefinance;

import android.content.Context;
import android.content.SharedPreferences;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class Amount {
    private final BigDecimal value;
    private final String currencySymbol;
    private final NumberFormat currencyFormatter;
    private final NumberFormat numberFormatter;
    private final Locale locale;

    public Amount(Context context, double rawValue) {
        this.locale = Locale.getDefault();
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String storedCurrency = sharedPreferences.getString("currency", "");
        this.currencySymbol = resolveCurrencySymbol(storedCurrency);
        this.value = BigDecimal.valueOf(rawValue).setScale(2, RoundingMode.HALF_UP);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        this.numberFormatter = NumberFormat.getNumberInstance(locale);
        this.numberFormatter.setGroupingUsed(false);
        configureFormatters();
    }

    public Amount(Context context, String rawString) {
        this.locale = Locale.getDefault();
        SharedPreferences sharedPreferences = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String storedCurrency = sharedPreferences.getString("currency", "");
        this.currencySymbol = resolveCurrencySymbol(storedCurrency);
        this.currencyFormatter = NumberFormat.getCurrencyInstance(locale);
        this.numberFormatter = NumberFormat.getNumberInstance(locale);
        this.numberFormatter.setGroupingUsed(false);
        configureFormatters();

        try {
            // Remove currency symbol or user input currency string from rawString
            String cleanedString = rawString.replace(currencySymbol, "").trim();

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
            char decimalSeparator = symbols.getDecimalSeparator();

            // Keep digits and decimal separator only
            cleanedString = cleanedString.replaceAll("[^\\d" + decimalSeparator + "]", "");
            if (cleanedString.isEmpty()) cleanedString = "0";

            Number parsedNumber = numberFormatter.parse(cleanedString);
            this.value = new BigDecimal(parsedNumber.toString())
                    .setScale(2, RoundingMode.HALF_UP);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Cannot parse amount: " + rawString, e);
        }
    }

    private void configureFormatters() {
        // Set fraction digits to 2 for amounts
        currencyFormatter.setMinimumFractionDigits(2);
        currencyFormatter.setMaximumFractionDigits(2);
        numberFormatter.setMinimumFractionDigits(2);
        numberFormatter.setMaximumFractionDigits(2);
    }

    private String resolveCurrencySymbol(String storedCurrency) {
        if (storedCurrency.length() == 3 && storedCurrency.matches("[A-Za-z]{3}")) {
            try {
                return java.util.Currency.getInstance(storedCurrency).getSymbol(locale);
            } catch (IllegalArgumentException e) {
                // Not a valid ISO code, treat as symbol string below
            }
        }
        // Treat storedCurrency as a symbol string (e.g. "₹", "₩", "zł")
        return storedCurrency;
    }

    public String getAmountString() {
        if (currencyFormatter.getCurrency() != null) {
            // If currencyFormatter has a currency, but our symbol differs, do manual formatting
            String formattedNumber = numberFormatter.format(value);
            return currencySymbol + formattedNumber;
        } else {
            // fallback: just prepend symbol manually
            return currencySymbol + numberFormatter.format(value);
        }
    }

    public String getFormattedAmountString() {
        numberFormatter.setGroupingUsed(true);
        return getAmountString();
    }

    public String getAmountStringWithoutCurrency() {
        return numberFormatter.format(value);
    }

    public double getAmountValue() {
        return value.doubleValue();
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public static void storeBalance(Context context, String value) {
        SharedPreferences prefs = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        prefs.edit().putString(Constants.SP_BALANCE, new Amount(context, value).getAmountString()).apply();
    }

    public static String getStoredBalance(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String value = prefs.getString(Constants.SP_BALANCE, zero());
        return new Amount(context, value).getAmountStringWithoutCurrency();
    }

    public static String zero() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.getDefault());
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(0);
    }
}