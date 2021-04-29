package com.tashila.mywalletfree.loans;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.tashila.mywalletfree.DatePickerFragment;
import com.tashila.mywalletfree.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DialogAddInvestment extends DialogFragment {
    private View view;
    private AlertDialog dialog;
    private TextInputLayout tilPerson;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDate;
    private TextInputLayout tilDetails;
    private EditText etPerson;
    private EditText etAmount;
    private EditText etDate;
    private EditText etDetails;
    private boolean isLent;
    private RadioGroup radioGroup;
    private DateTimeFormatter formatter;
    private static DialogAddInvestment instance;
    private Loan editingLoan;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        instance = this;

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_loan, null);
        tilPerson = view.findViewById(R.id.person);
        tilAmount = view.findViewById(R.id.amount);
        tilDate = view.findViewById(R.id.date);
        tilDetails = view.findViewById(R.id.details);
        etPerson = tilPerson.getEditText();
        etAmount = tilAmount.getEditText();
        etDate = tilDate.getEditText();
        etDetails = tilDetails.getEditText();
        isLent = true;
        radioGroup = view.findViewById(R.id.lentBorrowed);
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

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit loan dialog") == null) {
            builder.setView(view)
                    .setTitle(R.string.new_loan)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
        } else {
            builder.setView(view)
                    .setTitle(R.string.edit_loan)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            fillDetails(editingLoan);
        }

        return builder.create();
    }

    public DialogAddInvestment(Loan editingLoan) {
        this.editingLoan = editingLoan;
    }

    private void fillDetails(Loan editingLoan) {
        String person = editingLoan.getPerson();
        String amount = editingLoan.getAmount();
        String date;
        if (editingLoan.isLent())
            date = editingLoan.getLentDate();
        else {
            date = editingLoan.getDueDate();
            radioGroup.check(radioGroup.getChildAt(1).getId());
        }
        String details = editingLoan.getDetails();
        etPerson.setText(person);
        etAmount.setText(amount);
        etDate.setText(date);
        etDetails.setText(details);
    }

    public static DialogAddInvestment getInstance() {
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
        if (validatePerson() && validateAmount()) {
            String person = etPerson.getText().toString();
            String amount = etAmount.getText().toString();
            String lentDate = etDate.getText().toString();
            String details = etDetails.getText().toString();
            Loan loan = new Loan(isLent, !isLent, false, person, amount, null, lentDate, null, details);
            if (editingLoan != null) {
                loan.setId(editingLoan.getId());
                LoansFragment.getInstance().updateLoan(loan);
            } else {
                LoansFragment.getInstance().addLoan(loan);
                sharedPref.edit().putBoolean("haveLoans", true).apply();
            }
            dialog.dismiss();
        }
    }

    private boolean validatePerson() {
        if (etPerson.getText().toString().isEmpty()) {
            tilPerson.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilPerson.setError(null);
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
        bundle.putString("pickDate", "fromLoansFragment");
        DialogFragment loanDatePicker = new DatePickerFragment();
        loanDatePicker.setArguments(bundle);
        loanDatePicker.show(getActivity().getSupportFragmentManager(), "loan date picker");
    }

    public void setDate(String date) {
        etDate.setText(date);
    }

    private void setPaymentStatus(RadioGroup radioGroup) {
        RadioButton rbLent = (RadioButton) radioGroup.getChildAt(0);
        RadioButton rbBorrowed = (RadioButton) radioGroup.getChildAt(1);
        if (rbLent.isChecked()) {
            tilDate.setHint(getString(R.string.lent_date));
            isLent = true;
        }
        if (rbBorrowed.isChecked()) {
            tilDate.setHint(getString(R.string.borrowed_date));
            isLent = false;
        }
    }
}
