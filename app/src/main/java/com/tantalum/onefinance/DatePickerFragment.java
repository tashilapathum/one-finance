package com.tantalum.onefinance;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.tantalum.onefinance.bank.DialogBankInput;
import com.tantalum.onefinance.bills.DialogNewBill;
import com.tantalum.onefinance.investments.DialogAddInvestment;
import com.tantalum.onefinance.loans.DialogAddLoan;
import com.tantalum.onefinance.reports.ReportsActivity;
import com.tantalum.onefinance.transactions.DialogTransactionEditor;
import com.tantalum.onefinance.transactions.TransactionsActivity;
import com.tantalum.onefinance.wallet.DialogWalletInput;

public class DatePickerFragment extends DialogFragment {

    public static final String TAG = "DatePickerFragment";
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);

        // Create MaterialDatePicker
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select a date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(selection);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
            onDateSet(year, month, dayOfMonth);
        });

        datePicker.addOnDismissListener(dialogInterface -> getDialog().dismiss());
        datePicker.show(getActivity().getSupportFragmentManager(), TAG);
        return super.onCreateDialog(savedInstanceState);
    }

    public void onDateSet(int year, int month, int dayOfMonth) {
        month = month + 1; // Calendar.MONTH is zero-based
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        String date = formatter.format(LocalDate.of(year, month, dayOfMonth));
        LocalDateTime dateTime = LocalDateTime.of(year, month, dayOfMonth, LocalDateTime.now().getHour(), LocalDateTime.now().getMinute());
        ZoneId sriLankaZoneId = ZoneId.of("Asia/Colombo");
        ZonedDateTime zdt = dateTime.atZone(sriLankaZoneId);
        String dateInMillis = String.valueOf(zdt.toInstant().toEpochMilli());
        int week = new DateTimeHandler(dateInMillis).getWeekOfYear();
        int dayOfYear = new DateTimeHandler(dateInMillis).getDayOfYear();
        String fromContext = null;
        Bundle bundle = this.getArguments();
        if (bundle != null)
            fromContext = bundle.getString("pickDate", "fromWalletFragment");

        switch (fromContext) {
            case "fromWalletInput" -> DialogWalletInput.getInstance().setDate(dateInMillis);
            case "fromBankInput" -> DialogBankInput.getInstance().setDate(dateInMillis);
            case "fromBillsFragment" -> DialogNewBill.getInstance().setDate(date, Long.parseLong(dateInMillis), zdt.plusHours(23).toInstant().toEpochMilli());
            case "fromLoansFragment" -> DialogAddLoan.getInstance().setDate(date, Long.parseLong(dateInMillis), zdt.plusHours(23).toInstant().toEpochMilli());
            case "fromInvestmentsFragment" -> DialogAddInvestment.getInstance().setDate(date, Long.parseLong(dateInMillis));
            case "fromReports" -> ((ReportsActivity) getActivity()).applyFilter(year, month, week, dayOfYear, dayOfMonth);
            case "fromTransactionEditor" -> DialogTransactionEditor.getInstance().setDate(date, dateInMillis);
            case "fromTransactionFilter" -> {
                DateTimeHandler dateTimeHandler = new DateTimeHandler(dateInMillis);
                ((TransactionsActivity) getActivity()).filterByDate(dateTimeHandler.getDayOfYear());
            }
        }
    }
}