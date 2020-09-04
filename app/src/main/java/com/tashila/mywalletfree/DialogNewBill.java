package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private boolean isPaid;
    private boolean isMonthly;
    private RadioGroup radioGroup;
    private DateTimeFormatter formatter;
    private static DialogNewBill instance;
    private Bill editingBill;
    private CheckBox cbMonthly;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AndroidThreeTen.init(getActivity());
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

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
            } else
                BillsFragment.getInstance().addBill(bill);
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
            isPaid = true;
        }
        if (rbDue.isChecked()) {
            tilDate.setHint(getString(R.string.pay_due_date));
            isPaid = false;
        }
    }
}
