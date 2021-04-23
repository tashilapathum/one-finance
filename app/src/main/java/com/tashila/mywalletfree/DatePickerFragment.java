package com.tashila.mywalletfree;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.tashila.mywalletfree.bills.DialogNewBill;
import com.tashila.mywalletfree.reports.Reports;
import com.tashila.mywalletfree.transactions.DialogTransactionEditor;
import com.tashila.mywalletfree.transactions.TransactionHistory;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    public static final String TAG = "DatePickerFragment";
    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
        month = month + 1;
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        String date = formatter.format(LocalDate.of(year, month, dayOfMonth));
        LocalDateTime dateTime = LocalDateTime.of(year, month, dayOfMonth, LocalDateTime.now().getHour(), LocalDateTime.now().getHour());
        ZonedDateTime zdt = dateTime.atZone(ZoneId.systemDefault());
        String dateInMillis = String.valueOf(zdt.toInstant().toEpochMilli());
        int week = new DateTimeHandler(dateInMillis).getWeekOfYear();
        int dayOfYear = new DateTimeHandler(dateInMillis).getDayOfYear();
        String fromContext = null;
        Bundle bundle = this.getArguments();
        if (bundle != null)
            fromContext = bundle.getString("pickDate", "fromWalletFragment");

        if (fromContext.equals("fromWalletFragment")) {
            sharedPref.edit().putString("preDate", dateInMillis).apply();
            WalletFragment.getInstance().continueLongClickProcess();
        }

        if (fromContext.equals("fromBillsFragment"))
            DialogNewBill.getInstance().setDate(date);

        if (fromContext.equals("fromTransactionEditor"))
            DialogTransactionEditor.getInstance().setDate(date, dateInMillis);

        if (fromContext.equals("fromTransactionFilter")) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(dateInMillis);
            ((TransactionHistory) getActivity()).filterByDate(dateTimeHandler.getDayOfYear());
        }

        if (fromContext.equals("fromReports")) {
            ((Reports) getActivity()).applyFilter(year, month, week, dayOfYear, dayOfMonth);
        }
    }
}