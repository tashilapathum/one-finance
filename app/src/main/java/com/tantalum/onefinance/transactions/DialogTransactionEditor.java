package com.tantalum.onefinance.transactions;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;
import com.tantalum.onefinance.categories.CategoriesManager;

import java.text.DecimalFormat;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

public class DialogTransactionEditor extends BottomSheetDialogFragment {
    private View view;
    private TextInputLayout tilAmount;
    private TextInputLayout tilDescription;
    private TextInputLayout tilCategory;
    private TextInputLayout tilDate;
    private EditText etAmount;
    private EditText etDescription;
    private EditText etCategory;
    private EditText etDate;
    private TransactionItem transactionItem;
    private BottomSheetDialog dialog;
    private static DialogTransactionEditor instance;
    private SharedPreferences sharedPref;
    private String dateInMillis;
    private MaterialRadioButton rbIncome;
    private boolean isFromFragment;
    private TransactionsViewModel transactionsViewModel;
    private CategoriesManager categoriesManager;


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
        categoriesManager = new CategoriesManager(requireContext());

        Button btnSave = view.findViewById(R.id.save);
        Button btnCancel = view.findViewById(R.id.cancel);
        tilAmount = view.findViewById(R.id.amount);
        tilDescription = view.findViewById(R.id.description);
        tilCategory = view.findViewById(R.id.category);
        tilDate = view.findViewById(R.id.date);
        etAmount = tilAmount.getEditText();
        etDescription = tilDescription.getEditText();
        etCategory = tilCategory.getEditText();
        etDate = tilDate.getEditText();
        rbIncome = view.findViewById(R.id.income);

        fillDetails();

        btnSave.setOnClickListener(view -> save());
        btnCancel.setOnClickListener(view -> dialog.cancel());
        etCategory.setOnClickListener(view -> showCategoryPicker());
        etDate.setOnClickListener(view -> showDatePicker());

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
        etCategory.setText(transactionItem.getCategory().split("###")[0]);
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
            String description = etDescription.getText().toString().trim();
            String category = etCategory.getText().toString();
            transactionItem.setAmount(df.format(Double.parseDouble(newAmountStr)));
            transactionItem.setDescription(description);
            transactionItem.setCategory(category);

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

    /**Show only category names in the dialog but set the full category item to the transaction when selected*/
    private void showCategoryPicker() {
        List<String> categories = categoriesManager.getCategoryItems();
        List<String> categoryNames = categoriesManager.getCategoryNames();

        //find selected category index
        int selectedIndex = -1;
        for (int i = 0; i < categories.size(); i++)
            if (etCategory.getText().toString().equals(categoryNames.get(i))) {
                selectedIndex = i;
                break;
            }

        //show dialog
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.category)
                .setSingleChoiceItems(categoryNames.toArray(new String[0]), selectedIndex, (dialog, which) -> {
                    etCategory.setText(categoryNames.get(which));
                    transactionItem.setCategory(categories.get(which));
                    dialog.dismiss();
                })
                .show();
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
