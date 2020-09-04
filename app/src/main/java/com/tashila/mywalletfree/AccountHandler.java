package com.tashila.mywalletfree;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.math.BigDecimal;
import java.math.RoundingMode;

class AccountHandler {
    public static final String TAG = "AccountHandler";
    private Context context;
    private SharedPreferences sharedPref;

    AccountHandler(Context context) {
        this.context = context;
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
    }

    void setDetail(View view, String stringKey, boolean isEditText) {
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        String detail = sharedPref.getString(stringKey, null);
        if (isEditText) {
            EditText editText = (EditText) view;
            editText.setText(detail);
        } else {
            TextView textView = (TextView) view;
            textView.setText(detail);
        }
    }

    /*void calculateInterests() {
        String language = sharedPref.getString("language", "english");
        String currency = sharedPref.getString("currency", null);
        for (int accNo = 1; accNo <= 20; accNo++) {
            boolean isAccountAvailable = sharedPref.getBoolean("isAccountSlot" + accNo + "Taken", false);
            boolean accountHasInterest = sharedPref.getString("annualInterestStr" + accNo, null) != null
            && !sharedPref.getString("annualInterestStr" + accNo, null).isEmpty();
            if (isAccountAvailable && accountHasInterest) {
                int currentYear = LocalDate.now().getYear();
                int currentMonth = LocalDate.now().getMonthValue();
                int currentDate = LocalDate.now().getDayOfMonth();
                int endOfMonth = YearMonth.now().lengthOfMonth();

                //for the fist year of using the app
                if (!sharedPref.contains("firstYear"))
                    sharedPref.edit().putInt("fistYear", currentYear).apply();
                if (!sharedPref.contains("firstMonth"))
                    sharedPref.edit().putInt("fistMonth", currentMonth).apply();
                int firstYear = sharedPref.getInt("firstYear", 0);
                int firstMonth = sharedPref.getInt("firstMonth", 0);
                int sm; //starting month
                if (firstYear == currentYear) sm = firstMonth;
                else sm = 1;

                for (int m = sm; m <= currentMonth; m++) {

                    //when should this be performed?
                    boolean thisMonthNotAlreadyCalculated = !sharedPref.getBoolean("account" + accNo + "InterestOfYear"
                            + currentYear + "Month" + currentMonth + "Done", false);
                    boolean previousMonthNotAlreadyCalculated = !sharedPref.getBoolean("account" + accNo + "InterestOfYear"
                            + currentYear + "Month" + (LocalDate.now().minusMonths(1).getMonthValue()) + "Done", false);
                    boolean itsEndOfTheMonth = currentDate == endOfMonth;

                    if ((thisMonthNotAlreadyCalculated && itsEndOfTheMonth) || previousMonthNotAlreadyCalculated) {
                        double balance = Double.parseDouble(sharedPref.getString("accountBalance" + accNo, null));
                        double interestRate = 0;
                        double interest;
                        if (sharedPref.getBoolean("hasMultiInterests" + accNo, false)) {
                            //assign multi ranges and interests
                            int NIR = sharedPref.getInt("noOfInterestRanges" + accNo, 0);
                            for (int r = 1; r < NIR; r++) { //r -> row of min,max,interest
                                double min = Double.parseDouble(sharedPref.getString("account" + accNo + "MinAmount" + r, null));
                                double max = Double.parseDouble(sharedPref.getString("account" + accNo + "MaxAmount" + r, null));
                                double MI = Double.parseDouble(sharedPref.getString("account" + accNo + "MinAmount" + r, null));
                                if (balance >= min && balance <= max) {
                                    interestRate = MI / 12;
                                    break;
                                }
                            }
                        } else {
                            interestRate = Double.parseDouble(sharedPref.getString("annualInterestStr" + accNo, null)) / 12;
                        }

                        //prepare data
                        interest = balance * interestRate / 100;
                        double roundedInterest = BigDecimal.valueOf(interest).setScale(2, RoundingMode.HALF_UP).doubleValue();
                        String monthName = LocalDate.now().getMonth().toString();
                        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
                        String timeStamp = formatter.format(LocalDateTime.now());

                        //in case of previous month's
                        if (previousMonthNotAlreadyCalculated) {
                            //in case of January
                            if (currentMonth == 1) {
                                currentMonth = 12;
                                monthName = "DECEMBER";
                            }
                            else {
                                currentMonth--;
                                monthName = LocalDate.now().minusMonths(1).getMonth().toString();
                            }
                        }

                        //save the activity
                        String activity;
                        if (language.equalsIgnoreCase("english"))
                            activity = "Added " + currency + roundedInterest + " as the interest of " + monthName + "###" + timeStamp;
                        else {
                            switch (currentMonth) {
                                case 1: {
                                    monthName = "ජනවාරි";
                                    break;
                                }
                                case 2: {
                                    monthName = "පෙබරවාරි";
                                    break;
                                }
                                case 3: {
                                    monthName = "මාර්තු";
                                    break;
                                }
                                case 4: {
                                    monthName = "අප්‍රේල්";
                                    break;
                                }
                                case 5: {
                                    monthName = "මැයි";
                                    break;
                                }
                                case 6: {
                                    monthName = "ජූනි";
                                    break;
                                }
                                case 7: {
                                    monthName = "ජූලි";
                                    break;
                                }
                                case 8: {
                                    monthName = "අගෝස්තු";
                                    break;
                                }
                                case 9: {
                                    monthName = "සැප්තැම්බර්";
                                    break;
                                }
                                case 10: {
                                    monthName = "ඔක්තෝබර්";
                                    break;
                                }
                                case 11: {
                                    monthName = "නොවැම්බර්";
                                    break;
                                }
                                case 12: {
                                    monthName = "දෙසැම්බර්";
                                    break;
                                }
                            }
                            activity = monthName + " මස පොලිය ලෙස " + currency + roundedInterest + " ක් එකතු කරන ලදී###" + timeStamp;
                        }
                        String activities = sharedPref.getString("activities" + accNo, null);
                        activities = activities + "~~~" + activity;
                        sharedPref.edit().putString("activities" + accNo, activities).apply();

                        //update balance
                        balance = balance + roundedInterest;
                        sharedPref.edit().putString("accountBalance" + accNo, String.valueOf(balance)).apply();

                        //finalize
                        sharedPref.edit().putBoolean("account" + accNo + "InterestOfYear" + currentYear + "Month" + currentMonth + "Done", true).apply();
                        Log.i(TAG, "Calculate interest complete!");
                    }
                }
            }
        }
    }*/
}
