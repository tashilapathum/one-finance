package com.tashila.mywalletfree;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.DateTimeFormatter;
import org.threeten.bp.format.FormatStyle;

import java.text.DecimalFormat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DialogTransactionEditor extends BottomSheetDialogFragment {
    private View view;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescription;
    private TextInputLayout tilDate;
    private EditText etAmount;
    private EditText etDescription;
    private EditText etDate;
    private TransactionItem transactionItem;
    private Context context;
    private BottomSheetDialog dialog;
    private static DialogTransactionEditor instance;
    private String date;
    private SharedPreferences sharedPref;
    private String language;


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        instance = this;
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        view = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_transaction, null);
        language = sharedPref.getString("language", "english");
        dialog = new BottomSheetDialog(getActivity());
        dialog.setContentView(view);

        Button btnSave = view.findViewById(R.id.save);
        Button btnCancel = view.findViewById(R.id.cancel);
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilDate = view.findViewById(R.id.date);
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etDate = tilDate.getEditText();

        fillDetails();
        date = etDate.getText().toString();

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

    public DialogTransactionEditor(Context context, TransactionItem transactionItem) {
        this.context = context;
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
        Bundle bundle = this.getArguments();
        etAmount.setText(bundle.getString("amount"));
        etDescription.setText(bundle.getString("description"));
        String date = bundle.getString("date");
        if (date == null || date.equals("null"))
            tilDate.setVisibility(View.GONE);
        else {
            etDate.setText(date);
            etDate.setFocusable(false);
        }
    }

    private void save() {
        DecimalFormat df = new DecimalFormat("#.00");
        if (validateAmount() & validateDescription()) {
            String oldAmountStr = transactionItem.getAmount();
            String newAmountStr = etAmount.getText().toString();
            String description = etDescription.getText().toString();
            transactionItem.setAmount(df.format(Double.parseDouble(newAmountStr)));
            transactionItem.setDescription(description);

            //date info
            try {
                LocalDate localDate;
                DateTimeFormatter dateFormatter;
                if (language.equalsIgnoreCase("සිංහල")) {
                    dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                } else {
                    dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
                }
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);
                localDate = LocalDate.parse(date, dateFormatter);
                int dayOfWeek = localDate.getDayOfWeek().getValue();
                int dayOfMonth = localDate.getDayOfMonth();
                int monthOfYear = localDate.getMonthValue();
                transactionItem.setUserDate(date + " " + timeFormatter.format(LocalDateTime.now()));
                transactionItem.setDayOfWeek(dayOfWeek);
                transactionItem.setDayOfMonth(dayOfMonth);
                transactionItem.setMonthOfYear(monthOfYear);
            }
            catch (Exception e) {
                Toast.makeText(context, "Failed to update the date", Toast.LENGTH_SHORT).show();
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
            TransactionHistory transactionHistory = (TransactionHistory) context;
            transactionHistory.updateTransaction(transactionItem);
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

    public void setDate(String date) {
        this.date = date;
        etDate.setText(date);
    }
}
