package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DialogNewLoan extends DialogFragment {
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
    private static DialogNewLoan instance;
    private Loan editingLoan;
    private CheckBox cbMonthly;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        instance = this;

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_new_loan, null);
        tilTitle = view.findViewById(R.id.title);
        tilAmount = view.findViewById(R.id.amount);
        tilDate = view.findViewById(R.id.loanDate);
        tilRemarks = view.findViewById(R.id.remarks);
        etTitle = tilTitle.getEditText();
        etAmount = tilAmount.getEditText();
        etDate = tilDate.getEditText();
        etRemarks = tilRemarks.getEditText();
        isPaid = true;
        radioGroup = view.findViewById(R.id.rgPaidOrDue);
        cbMonthly = view.findViewById(R.id.cbMonthly);
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
        cbMonthly.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                isMonthly = isChecked;
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

    public DialogNewLoan(Loan editingLoan) {
        this.editingLoan = editingLoan;
    }

    private void fillDetails(Loan editingLoan) {
        String title = editingLoan.getTitle();
        String amount = editingLoan.getAmount();
        String date;
        if (editingLoan.isPaid())
            date = editingLoan.getPaidDate();
        else {
            date = editingLoan.getDueDate();
            radioGroup.check(radioGroup.getChildAt(1).getId());
        }
        if (editingLoan.isMonthly())
            cbMonthly.setChecked(true);
        String remarks = editingLoan.getRemarks();
        etTitle.setText(title);
        etAmount.setText(amount);
        etDate.setText(date);
        etRemarks.setText(remarks);
    }

    public static DialogNewLoan getInstance() {
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
            Loan loan = new Loan(isPaid, title, amount, paidDate, dueDate, remarks, isMonthly, lastPaidMonth);
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
        bundle.putString("pickDate", "fromLoansFragment");
        DialogFragment loanDatePicker = new DatePickerFragment();
        loanDatePicker.setArguments(bundle);
        loanDatePicker.show(getActivity().getSupportFragmentManager(), "loan date picker");
    }

    public void setDate(String date) {
        etDate.setText(date);
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
        }
    }
}
