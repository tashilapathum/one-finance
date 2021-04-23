package com.tashila.mywalletfree.bills;

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

import com.tashila.mywalletfree.AlertReceiver;
import com.tashila.mywalletfree.R;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class BillsAdapter extends ListAdapter<Bill, BillsAdapter.BillHolder> {
    public static final String TAG = "BillsAdapter";
    private Context context;
    private SharedPreferences sharedPref;
    private String currency;


    public BillsAdapter(Context context) {
        super(DIFF_CALLBACK);
        this.context = context;
        sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        currency = sharedPref.getString("currency", "");
    }

    private static final DiffUtil.ItemCallback<Bill> DIFF_CALLBACK = new DiffUtil.ItemCallback<Bill>() {
        @Override
        public boolean areItemsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public BillHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_bill, parent, false);
        return new BillHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BillHolder holder, int position) {
        final Bill currentBill = getItem(position);
        holder.tvTitle.setText(currentBill.getTitle());
        if (currentBill.isMonthly())
            holder.tvAmount.setText(currency + currentBill.getAmount() + " (" + context.getResources().getString(R.string.monthly) + ")");
        else
            holder.tvAmount.setText(currency + currentBill.getAmount());
        holder.tvPaidDate.setText(currentBill.getPaidDate());
        holder.tvDueDate.setText(currentBill.getDueDate());
        holder.tvRemarks.setText(currentBill.getRemarks());
        holder.imMarkAsPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().markAsPaid(currentBill);
            }
        });
        holder.imMarkUnpaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().markUnpaid(currentBill);
            }
        });
        holder.imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().editBill(currentBill);
            }
        });
        holder.imDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BillsFragment.getInstance().deleteBill(currentBill);
            }
        });

        //toggle due and paid buttons
        if (currentBill.isPaid()) {
            holder.imMarkAsPaid.setVisibility(View.GONE);
            holder.imMarkUnpaid.setVisibility(View.VISIBLE);
        } else {
            holder.imMarkAsPaid.setVisibility(View.VISIBLE);
            holder.imMarkUnpaid.setVisibility(View.GONE);
        }

        //show and hide "overdue"
        //TODO: convert this to millis method
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
        int today = LocalDate.now().getDayOfYear();
        int dueDay;
        try {
            if (!currentBill.getDueDate().equals("N/A")) {
                dueDay = LocalDate.parse(currentBill.getDueDate(), formatter).getDayOfYear();
                setNotifications(dueDay);
                if (today > dueDay && !currentBill.isPaid())
                    holder.tvOverdue.setVisibility(View.VISIBLE);
                else
                    holder.tvOverdue.setVisibility(View.GONE);
            }
        } catch (Exception e) { //because the user might change the localization
            e.printStackTrace();
        }

        //renew monthly payments
        if (currentBill.isMonthly() && currentBill.getLastPaidMonth() < LocalDate.now().getMonthValue())
            currentBill.setPaid(false);
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


    class BillHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvAmount;
        private TextView tvPaidDate;
        private TextView tvDueDate;
        private TextView tvRemarks;
        private TextView tvOverdue;
        private ImageButton imMarkAsPaid;
        private ImageButton imEdit;
        private ImageButton imDelete;
        private ImageButton imMarkUnpaid;
        private LinearLayout invisible_part;

        public BillHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.title);
            tvAmount = itemView.findViewById(R.id.amount);
            tvPaidDate = itemView.findViewById(R.id.settledDate);
            tvDueDate = itemView.findViewById(R.id.dueDate);
            tvRemarks = itemView.findViewById(R.id.remarks);
            tvOverdue = itemView.findViewById(R.id.overdue);
            imMarkAsPaid = itemView.findViewById(R.id.markAsPaid);
            imEdit = itemView.findViewById(R.id.edit);
            imDelete = itemView.findViewById(R.id.delete);
            imMarkUnpaid = itemView.findViewById(R.id.markUnpaid);
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
