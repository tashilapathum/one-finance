package com.tashila.mywalletfree.bills;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.tashila.mywalletfree.DatePickerFragment;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.TimeZone;

public class DialogNewBill extends DialogFragment {
    private View view;
    private AlertDialog dialog;
    private TextInputLayout tilTitle;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDate;
    private TextInputLayout tilRemarks;
    private EditText etTitle;
    private EditText etAmount;
    private EditText etDate;
    private EditText etRemarks;
    private boolean isPaid;
    private boolean isMonthly;
    private RadioGroup radioGroup;
    private DateTimeFormatter formatter;
    private static DialogNewBill instance;
    private Bill editingBill;
    private CheckBox cbMonthly;
    private CheckBox cbCalendar;
    private boolean addToCalendar = true;
    private SharedPreferences sharedPref;
    private long dateInMillis;
    private long dateEndInMillis;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        instance = this;

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_bill, null);
        tilTitle = view.findViewById(R.id.title);
        tilAmount = view.findViewById(R.id.amount);
        tilDate = view.findViewById(R.id.billDate);
        tilRemarks = view.findViewById(R.id.remarks);
        etTitle = tilTitle.getEditText();
        etAmount = tilAmount.getEditText();
        etDate = tilDate.getEditText();
        etRemarks = tilRemarks.getEditText();
        isPaid = true;
        radioGroup = view.findViewById(R.id.rgPaidOrDue);
        cbMonthly = view.findViewById(R.id.cbMonthly);
        cbCalendar = view.findViewById(R.id.calendarCheck);
        setDate(
                LocalDate.now().format(formatter),
                new DateTimeHandler(LocalDate.now()).getInMillis(),
                new DateTimeHandler(LocalDate.now().atStartOfDay().plusHours(23)).getInMillis()
        );
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                setPaymentStatus(radioGroup);
            }
        });
        cbMonthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isMonthly = isChecked;
            }
        });
        cbCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addToCalendar = isChecked;
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit bill dialog") == null) {
            builder.setView(view)
                    .setTitle(R.string.new_bill)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
        } else {
            builder.setView(view)
                    .setTitle(R.string.edit_bill)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            cbCalendar.setVisibility(View.GONE);
            fillDetails(editingBill);
        }

        return builder.create();
    }

    public DialogNewBill(Bill editingBill) {
        this.editingBill = editingBill;
    }

    private void fillDetails(Bill editingBill) {
        String title = editingBill.getTitle();
        String amount = editingBill.getAmount();
        String date;
        if (editingBill.isPaid())
            date = editingBill.getPaidDate();
        else {
            date = editingBill.getDueDate();
            radioGroup.check(radioGroup.getChildAt(1).getId());
        }
        if (editingBill.isMonthly())
            cbMonthly.setChecked(true);
        String remarks = editingBill.getRemarks();
        etTitle.setText(title);
        etAmount.setText(amount);
        etDate.setText(date);
        etRemarks.setText(remarks);
    }

    public static DialogNewBill getInstance() {
        return instance;
    }

    @Override
    public void onResume() {
        super.onResume();
        dialog = (AlertDialog) getDialog();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddOrEdit();
            }
        });
    }

    private void onClickAddOrEdit() {
        if (validateTitle() && validateAmount()) {
            String title = etTitle.getText().toString();
            String amount = etAmount.getText().toString();
            String paidDate = null;
            String dueDate = null;
            int lastPaidMonth = -1; //to check if this was set
            if (isPaid) {
                paidDate = etDate.getText().toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
                try {
                    lastPaidMonth = LocalDate.parse(paidDate, formatter).getMonthValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else
                dueDate = etDate.getText().toString();
            String remarks = etRemarks.getText().toString();
            Bill bill = new Bill(isPaid, title, amount, paidDate, dueDate, remarks, isMonthly, lastPaidMonth);
            if (editingBill != null) {
                bill.setId(editingBill.getId());
                BillsFragment.getInstance().updateBill(bill);
            } else {
                BillsFragment.getInstance().addBill(bill);
                if (addToCalendar) addToCalendar();
                sharedPref.edit().putBoolean("haveBills", true).apply();
            }
            dialog.dismiss();
        }
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dateInMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dateEndInMillis)
                .putExtra(CalendarContract.Events.TITLE, etTitle.getText().toString())
                .putExtra(CalendarContract.Events.DESCRIPTION, etRemarks.getText().toString())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    private boolean validateTitle() {
        if (etTitle.getText().toString().isEmpty()) {
            tilTitle.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilTitle.setError(null);
            return true;
        }
    }

    private boolean validateAmount() {
        if (etAmount.getText().toString().isEmpty()) {
            tilAmount.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilAmount.setError(null);
            return true;
        }
    }

    private void showDatePicker() {
        Bundle bundle = new Bundle();
        bundle.putString("pickDate", "fromBillsFragment");
        DialogFragment billDatePicker = new DatePickerFragment();
        billDatePicker.setArguments(bundle);
        billDatePicker.show(getActivity().getSupportFragmentManager(), "bill date picker");
    }

    public void setDate(String date, long dateInMillis, long dateEndInMillis) {
        etDate.setText(date);
        this.dateInMillis = dateInMillis;
        this.dateEndInMillis = dateEndInMillis;
    }

    private void setPaymentStatus(RadioGroup radioGroup) {
        RadioButton rbPaid = (RadioButton) radioGroup.getChildAt(0);
        RadioButton rbDue = (RadioButton) radioGroup.getChildAt(1);
        if (rbPaid.isChecked()) {
            tilDate.setHint(getString(R.string.paid_date));
            isPaid = true;
        }
        if (rbDue.isChecked()) {
            tilDate.setHint(getString(R.string.pay_due_date));
            isPaid = false;
            cbCalendar.setVisibility(View.VISIBLE);
        }
    }
}
