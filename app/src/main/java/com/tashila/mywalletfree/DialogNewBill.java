package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputLayout;
import com.jakewharton.threetenabp.AndroidThreeTen;

import org.threeten.bp.LocalDate;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

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
    private String paymentStatus;
    private RadioGroup radioGroup;
    private DateTimeFormatter formatter;
    private static DialogNewBill instance;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AndroidThreeTen.init(getActivity());
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        instance = this;

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_bill, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle("New bill")
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setNegativeButton(R.string.cancel, null);

        tilTitle = view.findViewById(R.id.billTitle);
        tilAmount = view.findViewById(R.id.billAmount);
        tilDate = view.findViewById(R.id.billDate);
        tilRemarks = view.findViewById(R.id.billRemarks);
        etTitle = tilTitle.getEditText();
        etAmount = tilAmount.getEditText();
        etDate = tilDate.getEditText();
        etRemarks = tilRemarks.getEditText();
        paymentStatus = "paid";
        radioGroup = view.findViewById(R.id.rgPaidOrDue);

        setDate(LocalDate.now().format(formatter));
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

        return builder.create();
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
                onClickAdd();
            }
        });
    }

    private void onClickAdd() {
        if (validateTitle() && validateAmount()) {
            String title = etTitle.getText().toString();
            String amount = etAmount.getText().toString();
            String date = etDate.getText().toString();
            String remarks = etRemarks.getText().toString();
            BillsFragment.getInstance().addBill(title, amount, date, remarks, paymentStatus);
            dialog.dismiss();
        }
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
        bundle.putString("pickDate", "fromCartFragment");
        DialogFragment billDatePicker = new DatePickerFragment();
        billDatePicker.setArguments(bundle);
        billDatePicker.show(getActivity().getSupportFragmentManager(), "bill date picker");
    }

    public void setDate(String date) {
        etDate.setText(date);
    }

    private void setPaymentStatus(RadioGroup radioGroup) {
        RadioButton rbPaid = (RadioButton) radioGroup.getChildAt(0);
        RadioButton rbDue = (RadioButton) radioGroup.getChildAt(1);
        if (rbPaid.isChecked()) {
            tilDate.setHint(getString(R.string.paid_date));
            paymentStatus = "paid";
        }
        if (rbDue.isChecked()) {
            tilDate.setHint(getString(R.string.pay_due_date));
            paymentStatus = "due";
        }
    }

}
