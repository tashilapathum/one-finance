package com.tantalum.onefinance;

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

import com.tantalum.onefinance.bills.DialogNewBill;
import com.tantalum.onefinance.investments.DialogAddInvestment;
import com.tantalum.onefinance.loans.DialogAddLoan;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.transactions.DialogTransactionEditor;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.wallet.DialogWalletInput;

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

        if (fromContext.equals("fromWalletInput"))
            DialogWalletInput.getInstance().setDate(dateInMillis);

        if (fromContext.equals("fromBillsFragment"))
            DialogNewBill.getInstance().setDate(date, Long.parseLong(dateInMillis), zdt.plusHours(23).toInstant().toEpochMilli());

        if (fromContext.equals("fromLoansFragment"))
            DialogAddLoan.getInstance().setDate(date, Long.parseLong(dateInMillis), zdt.plusHours(23).toInstant().toEpochMilli());

        if (fromContext.equals("fromInvestmentsFragment"))
            DialogAddInvestment.getInstance().setDate(date, Long.parseLong(dateInMillis));

        if (fromContext.equals("fromTransactionEditor"))
            DialogTransactionEditor.getInstance().setDate(date, dateInMillis);

        if (fromContext.equals("fromTransactionFilter")) {
            DateTimeHandler dateTimeHandler = new DateTimeHandler(dateInMillis);
            ((TransactionsActivity) getActivity()).filterByDate(dateTimeHandler.getDayOfYear());
        }

        if (fromContext.equals("fromReports")) {
            ((ReportsActivity) getActivity()).applyFilter(year, month, week, dayOfYear, dayOfMonth);
        }
    }
}