package com.tashila.mywalletfree.investments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;
import com.tashila.mywalletfree.DatePickerFragment;
import com.tashila.mywalletfree.DateTimeHandler;
import com.tashila.mywalletfree.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class DialogAddInvestment extends BottomSheetDialogFragment {
    private View view;
    private BottomSheetDialog dialog;
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
    private long dateInMillis;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        dateInMillis = System.currentTimeMillis();
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
        setDate(LocalDate.now().format(formatter), dateInMillis);
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        dialog = new BottomSheetDialog(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit investment dialog") == null) {
            dialog.setContentView(view);
        } else {
            dialog.setContentView(view);
            ((Button) view.findViewById(R.id.add)).setText(R.string.save);
            fillDetails(editingInvestment);
        }

        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        view.findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickAddOrEdit();
            }
        });

        return dialog;
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

    private void onClickAddOrEdit() {
        if (validateTitle() && validateAmount()) {
            String title = etTitle.getText().toString();
            String amount = etInvAmount.getText().toString();
            String description = etDescription.getText().toString();
            String tag = etTag.getText().toString();
            if (editingInvestment != null) { //when updating existing investment
                Investment investment = new Investment(title, description, Double.parseDouble(amount),
                        editingInvestment.getReturnValue(), dateInMillis, tag, editingInvestment.getHistory());
                investment.setId(editingInvestment.getId());
                InvestmentsFragment.getInstance().updateInvestment(investment);
                InvestmentView.getInstance().showDetails(investment);
            } else { //when creating new investment
                List<String> history = new ArrayList<>();
                history.add(getString(R.string.created_investment) + "###" + dateInMillis);
                Investment investment = new Investment(title, description, Double.parseDouble(amount), 0, dateInMillis, tag, history);
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

    public void setDate(String date, long dateInMillis) {
        this.dateInMillis = dateInMillis;
        etDate.setText(date);
    }
}
