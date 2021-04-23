package com.tashila.mywalletfree;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.lhoyong.library.SmoothCheckBox;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class LoansAdapter extends ListAdapter<Loan, LoansAdapter.LoanHolder> {
    public static final String TAG = "LoansAdapter";
    private Context context;
    private SharedPreferences sharedPref;
    private String currency;


    public LoansAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
    }

    private static final DiffUtil.ItemCallback<Loan> DIFF_CALLBACK = new DiffUtil.ItemCallback<Loan>() {
        @Override
        public boolean areItemsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public LoanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_loan, parent, false);
        return new LoanHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanHolder holder, int position) {
        final Loan currentLoan = getItem(position);
        holder.tvTitle.setText(currentLoan.getPerson());
        holder.tvAmount.setText(currency + currentLoan.getAmount());
        holder.tvSettledDate.setText(currentLoan.getSettledDate());
        holder.tvDueDate.setText(currentLoan.getDueDate());
        holder.tvRemarks.setText(currentLoan.getDetails());

        holder.cbMarkSettled.setOnCheckedChangeListener(new SmoothCheckBox.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull SmoothCheckBox smoothCheckBox, boolean isChecked) {
                LoansFragment.getInstance().toggleSettled(currentLoan, isChecked);
            }
        });
        holder.imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoansFragment.getInstance().editLoan(currentLoan);
            }
        });
        holder.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoansFragment.getInstance().deleteLoan(currentLoan);
            }
        });

        //toggle isSettled
        if (currentLoan.isSettled())
            holder.cbMarkSettled.setChecked(true, true);

        //show and hide "overdue"
        //TODO: convert this to millis method
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        int today = LocalDate.now().getDayOfYear();
        int dueDay;
        try {
            if (!currentLoan.getDueDate().equals("N/A")) {
                dueDay = LocalDate.parse(currentLoan.getDueDate(), formatter).getDayOfYear();
                setNotifications(dueDay);
                if (today > dueDay && !currentLoan.isSettled())
                    holder.tvOverdue.setVisibility(View.VISIBLE);
                else
                    holder.tvOverdue.setVisibility(View.GONE);
            }
        } catch (Exception e) { //because the user might change the localization
            e.printStackTrace();
        }
    }

    private void setNotifications(int dueDayOfYear) {
        LocalDate dueDate = LocalDate.ofYearDay(LocalDate.now().getYear(), dueDayOfYear);
        LocalDateTime prevDay = dueDate.minusDays(1).atStartOfDay().plusHours(13).plusMinutes(30);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlertReceiver.class);
        int reqCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, prevDay.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), pendingIntent);
    }


    class LoanHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvAmount;
        private TextView tvSettledDate;
        private TextView tvDueDate;
        private TextView tvRemarks;
        private TextView tvOverdue;
        private ImageButton imEdit;
        private ImageButton imDelete;
        private SmoothCheckBox cbMarkSettled;
        private LinearLayout invisible_part;

        public LoanHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvAmount = itemView.findViewById(R.id.amount);
            tvSettledDate = itemView.findViewById(R.id.settledDate);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvRemarks = itemView.findViewById(R.id.remarks);
            tvOverdue = itemView.findViewById(R.id.overdue);
            cbMarkSettled = itemView.findViewById(R.id.markAsSettled);
            imEdit = itemView.findViewById(R.id.edit);
            imDelete = itemView.findViewById(R.id.delete);
            invisible_part = itemView.findViewById(R.id.invisible_part);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleVisibility();
                }
            });
        }

        private void toggleVisibility() {
            if (invisible_part.getVisibility() == View.GONE)
                invisible_part.setVisibility(View.VISIBLE);
            else
                invisible_part.setVisibility(View.GONE);
        }
    }
}
