package com.tantalum.onefinance.transactions;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogTransactionEditor extends BottomSheetDialogFragment {
    private View view;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescription;
    private TextInputLayout tilDate;
    private EditText etAmount;
    private EditText etDescription;
    private EditText etDate;
    private TransactionItem transactionItem;
    private BottomSheetDialog dialog;
    private static DialogTransactionEditor instance;
    private SharedPreferences sharedPref;
    private String dateInMillis;
    private RadioGroup radioGroup;
    private MaterialRadioButton rbExpense;
    private MaterialRadioButton rbIncome;
    private boolean isFromFragment;
    private TransactionsViewModel transactionsViewModel;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        instance = this;
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_transaction, null);
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);
        transactionsViewModel = new ViewModelProvider(getActivity(), ViewModelProvider.AndroidViewModelFactory
                .getInstance(getActivity().getApplication())).get(TransactionsViewModel.class);

        Button btnSave = view.findViewById(R.id.save);
        Button btnCancel = view.findViewById(R.id.cancel);
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilDate = view.findViewById(R.id.date);
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etDate = tilDate.getEditText();
        radioGroup = view.findViewById(R.id.radioGroup);
        rbExpense = view.findViewById(R.id.expense);
        rbIncome = view.findViewById(R.id.income);

        fillDetails();

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                save();
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });
        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePicker();
            }
        });

        return dialog;
    }

    public DialogTransactionEditor(boolean isFromFragment, TransactionItem transactionItem) {
        this.isFromFragment = isFromFragment;
        this.transactionItem = transactionItem;
    }

    public static DialogTransactionEditor getInstance() {
        return instance;
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

    private boolean validateDescription() {
        if (etDescription.getText().toString().isEmpty()) {
            tilDescription.setError(getActivity().getResources().getString(R.string.required));
            return false;
        } else {
            tilDescription.setError(null);
            return true;
        }
    }

    private void fillDetails() {
        etAmount.setText(transactionItem.getAmount());
        etDescription.setText(transactionItem.getDescription());
        String date = new DateTimeHandler(transactionItem.getTimeInMillis()).getTimestamp();
        etDate.setText(date);
        etDate.setFocusable(false);
        String prefix = transactionItem.getPrefix();
        if (prefix.equals("+"))
            rbIncome.setChecked(true);
    }

    private void save() {
        DecimalFormat df = new DecimalFormat("#.00");
        if (validateAmount() & validateDescription()) {
            String oldAmountStr = transactionItem.getAmount();
            String newAmountStr = etAmount.getText().toString().replace(",", ".");
            String description = etDescription.getText().toString();
            if (!transactionItem.getAmount().equals(df.format(Double.parseDouble(newAmountStr))))
                transactionItem.setAmount(df.format(Double.parseDouble(newAmountStr)));
            if (!transactionItem.getDescription().equals(description))
                transactionItem.setDescription(description);

            if (dateInMillis != null) {
                if (new DateTimeHandler(transactionItem.getTimeInMillis()).getDayOfYear() != new DateTimeHandler(dateInMillis).getDayOfYear())
                    transactionItem.setTimeInMillis(dateInMillis);
                else
                    transactionItem.setTimeInMillis(String.valueOf(System.currentTimeMillis()));
            }

            //update balance
            double balance = Double.parseDouble(sharedPref.getString("balance", "0"));
            double newAmount = Double.parseDouble(newAmountStr);
            double oldAmount = Double.parseDouble(oldAmountStr);
            String prefix = transactionItem.getPrefix();
            if (prefix.equals("+")) {
                if (newAmount > oldAmount)
                    balance = balance + (newAmount - oldAmount);
                else
                    balance = balance - (oldAmount - newAmount);
            } else {
                if (newAmount > oldAmount)
                    balance = balance - (newAmount - oldAmount);
                else
                    balance = balance + (oldAmount - newAmount);
            }

            String newBalance = df.format(balance);
            sharedPref.edit().putString("balance", newBalance).apply();

            //save changes
            if (isFromFragment)
                TransactionsFragment.getInstance().updateTransaction(transactionItem);
            else
                ((TransactionsActivity) getActivity()).updateTransaction(transactionItem);
            dialog.cancel();
        } else {
            TextView tvBottomNote = view.findViewById(R.id.bottomNote);
            tvBottomNote.setVisibility(View.VISIBLE);
        }
    }

    private void showDatePicker() {
        Bundle bundle = new Bundle();
        bundle.putString("pickDate", "fromTransactionEditor");
        DialogFragment datePicker = new DatePickerFragment();
        datePicker.setArguments(bundle);
        datePicker.show(getActivity().getSupportFragmentManager(), "date picker dialog");
    }

    public void setDate(String date, String dateInMillis) {
        etDate.setText(date);
        this.dateInMillis = dateInMillis;
    }
}
