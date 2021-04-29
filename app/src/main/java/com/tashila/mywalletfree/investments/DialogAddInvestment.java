package com.tashila.mywalletfree.investments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.tashila.mywalletfree.DatePickerFragment;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DialogAddInvestment extends DialogFragment {
    private View view;
    private AlertDialog dialog;
    private TextInputLayout tilTitle;
    private TextInputLayout tilInvAmount;
    private TextInputLayout tilDate;
    private TextInputLayout tilDescription;
    private TextInputLayout tilTag;
    private EditText etTitle;
    private EditText etInvAmount;
    private EditText etDate;
    private EditText etDescription;
    private EditText etTag;
    private DateTimeFormatter formatter;
    private static DialogAddInvestment instance;
    private Investment editingInvestment;
    private SharedPreferences sharedPref;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        instance = this;

        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_add_investment, null);
        tilTitle = view.findViewById(R.id.title);
        tilInvAmount = view.findViewById(R.id.amount);
        tilDate = view.findViewById(R.id.date);
        tilDescription = view.findViewById(R.id.description);
        tilTag = view.findViewById(R.id.tag);
        etTitle = tilTitle.getEditText();
        etInvAmount = tilInvAmount.getEditText();
        etDate = tilDate.getEditText();
        etDescription = tilDescription.getEditText();
        etTag = tilTag.getEditText();
        setDate(LocalDate.now().format(formatter));
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit investment dialog") == null) {
            builder.setView(view)
                    .setTitle(R.string.add_investment)
                    .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
        } else {
            builder.setView(view)
                    .setTitle(R.string.edit_investment)
                    .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //handled in onResume
                        }
                    })
                    .setNegativeButton(R.string.cancel, null);
            fillDetails(editingInvestment);
        }

        return builder.create();
    }

    public DialogAddInvestment(Investment editingInvestment) {
        this.editingInvestment = editingInvestment;
    }

    private void fillDetails(Investment editingInvestment) {
        String person = editingInvestment.getTitle();
        String amount = String.valueOf(editingInvestment.getInvestValue());
        String date = new DateTimeHandler(String.valueOf(editingInvestment.getDateInMillis())).getDateStamp();
        String description = editingInvestment.getDescription();
        String tag = editingInvestment.getTag();

        etTitle.setText(person);
        etInvAmount.setText(amount);
        etDate.setText(date);
        etDescription.setText(description);
        etTag.setText(tag);
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
        if (validateTitle() && validateAmount()) {
            String title = etTitle.getText().toString();
            String amount = etInvAmount.getText().toString();
            long dateInMillis = new DateTimeHandler(
                    LocalDate.parse(etDate.getText().toString(), DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT))
                    .atStartOfDay()
            ).getInMillis();
            String description = etDescription.getText().toString();
            String tag = etTag.getText().toString();
            Investment investment = new Investment(title, description, Double.parseDouble(amount), 0, 0, dateInMillis, tag, null);
            if (editingInvestment != null) {
                investment.setId(editingInvestment.getId());
                InvestmentsFragment.getInstance().updateInvestment(investment);
            } else {
                InvestmentsFragment.getInstance().addInvestment(investment);
                sharedPref.edit().putBoolean("haveInvestments", true).apply();
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
        if (etInvAmount.getText().toString().isEmpty()) {
            tilInvAmount.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilInvAmount.setError(null);
            return true;
        }
    }

    private void showDatePicker() {
        Bundle bundle = new Bundle();
        bundle.putString("pickDate", "fromInvestmentsFragment");
        DialogFragment investmentDatePicker = new DatePickerFragment();
        investmentDatePicker.setArguments(bundle);
        investmentDatePicker.show(getActivity().getSupportFragmentManager(), "investments date picker");
    }

    public void setDate(String date) {
        etDate.setText(date);
    }
}
