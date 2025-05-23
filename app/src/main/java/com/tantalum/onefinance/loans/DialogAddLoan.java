package com.tantalum.onefinance.loans;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputLayout;
import com.tantalum.onefinance.DatePickerFragment;
import com.tantalum.onefinance.DateTimeHandler;
import com.tantalum.onefinance.R;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class DialogAddLoan extends BottomSheetDialogFragment {
    private View view;
    private BottomSheetDialog dialog;
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
    private CheckBox cbCalendar;
    private DateTimeFormatter formatter;
    private static DialogAddLoan instance;
    private Loan editingLoan;
    private final boolean isBorrowing;
    private boolean addToCalendar = false;
    private SharedPreferences sharedPref;
    private long dateInMillis;
    private long dateEndInMillis;
    private String loanTitle;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        sharedPref = getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);
        formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        dateInMillis = System.currentTimeMillis();
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
        radioGroup = view.findViewById(R.id.lentBorrowed);
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
        if (isBorrowing)
            ((RadioButton) radioGroup.getChildAt(0)).setChecked(true);
        else {
            ((RadioButton) radioGroup.getChildAt(1)).setChecked(true);
            isLent = true;
        }

        cbCalendar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                addToCalendar = isChecked;
            }
        });

        dialog = new BottomSheetDialog(getActivity());

        if (getActivity().getSupportFragmentManager().findFragmentByTag("edit loan dialog") == null) {
            dialog.setContentView(view);
        } else {
            dialog.setContentView(view);
            ((Button) view.findViewById(R.id.add)).setText(R.string.save);
            fillDetails(editingLoan);
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

    public DialogAddLoan(Loan editingLoan, boolean isBorrowing) {
        this.editingLoan = editingLoan;
        this.isBorrowing = isBorrowing;
    }

    private void fillDetails(Loan editingLoan) {
        String person = editingLoan.getPerson();
        String amount = editingLoan.getAmount();
        String date = editingLoan.getLentDate();
        String details = editingLoan.getDetails();

        if (!editingLoan.isLent())
            radioGroup.check(radioGroup.getChildAt(0).getId());
        etPerson.setText(person);
        etAmount.setText(amount);
        etDate.setText(date);
        etDetails.setText(details);
    }

    public static DialogAddLoan getInstance() {
        return instance;
    }

    private void onClickAddOrEdit() {
        if (validatePerson() && validateAmount()) {
            String person = etPerson.getText().toString().trim();
            String amount = etAmount.getText().toString().replace(",", ".");
            String lentDate = etDate.getText().toString();
            String details = etDetails.getText().toString().trim();

            Loan loan = new Loan(isLent, !isLent, false, person, amount, null, lentDate, null, details);
            if (editingLoan != null) {
                loan.setId(editingLoan.getId());
                LoansFragment.getInstance().updateLoan(loan);
            } else {
                LoansFragment.getInstance().addLoan(loan);
                if (addToCalendar) addToCalendar();
            }
            dialog.dismiss();
        }
    }

    private void addToCalendar() {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, dateInMillis)
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, dateEndInMillis)
                .putExtra(CalendarContract.Events.TITLE, loanTitle)
                .putExtra(CalendarContract.Events.DESCRIPTION, etDetails.getText().toString())
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
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

    public void setDate(String date, long dateInMillis, long dateEndInMillis) {
        etDate.setText(date);
        this.dateInMillis = dateInMillis;
        this.dateEndInMillis = dateEndInMillis;
    }

    private void setPaymentStatus(RadioGroup radioGroup) {
        RadioButton rbLent = (RadioButton) radioGroup.getChildAt(1);
        RadioButton rbBorrowed = (RadioButton) radioGroup.getChildAt(0);
        if (rbLent.isChecked()) {
            tilDate.setHint(getString(R.string.lent_date));
            loanTitle = getString(R.string.lent_prefix) + etPerson.getText().toString() + getString(R.string.lent_suffix_si);
            isLent = true;
        }
        if (rbBorrowed.isChecked()) {
            tilDate.setHint(getString(R.string.borrowed_date));
            loanTitle = getString(R.string.borrow_prefix) + etPerson.getText().toString() + getString(R.string.borrow_suffix_si);
            isLent = false;
        }
    }
}
