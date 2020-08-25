package com.tashila.mywalletfree;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.DatePicker;

import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    Calendar c = Calendar.getInstance();
    int year = c.get(Calendar.YEAR);
    int month = c.get(Calendar.MONTH);
    int day = c.get(Calendar.DAY_OF_MONTH);
    SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AndroidThreeTen.init(getActivity());
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int day, int month, int year) {
        month = month + 1;
        String fromContext = null;
        Bundle bundle = this.getArguments();
        if (bundle != null)
            fromContext = bundle.getString("pickDate", "fromWalletFragment");

        if (fromContext.equals("fromWalletFragment")) {
            LocalDateTime date = LocalDateTime.of(day, month, year,
                    LocalDateTime.now().getHour(), LocalDateTime.now().getMinute());
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT);
            String preDate = date.format(formatter);
            sharedPref.edit().putString("preDate", preDate).apply();
            WalletFragment.getInstance().continueLongClickProcess();
        }

        if (fromContext.equals("fromCartFragment")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            String date = LocalDate.of(day, month, year).format(formatter);
            DialogNewBill.getInstance().setDate(date);
        }

        if (fromContext.equals("fromTransactionEditor")) {
            DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
            String date = LocalDate.of(day, month, year).format(formatter);
            DialogTransactionEditor.getInstance().setDate(date);
        }
    }
}
