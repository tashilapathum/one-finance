package com.tashila.mywalletfree;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class AccountsAdapter extends ListAdapter<Account, AccountsAdapter.AccountHolder> {
    private Context context;
    private String theme;
    private AccountsViewModel accountsViewModel;
    private String currency;

    public AccountsAdapter(Activity activity) {
        super(DIFF_CALLBACK);
        this.context = activity;
        SharedPreferences sharedPref = activity.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        theme = sharedPref.getString("theme", "light");
        currency = sharedPref.getString("currency", "");
        accountsViewModel = new ViewModelProvider((AccountManager) activity, ViewModelProvider.AndroidViewModelFactory
                .getInstance(activity.getApplication())).get(AccountsViewModel.class);
    }

    private static final DiffUtil.ItemCallback<Account> DIFF_CALLBACK = new DiffUtil.ItemCallback<Account>() {
        @Override
        public boolean areItemsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
            return false;
        }

        @Override
        public boolean areContentsTheSame(@NonNull Account oldItem, @NonNull Account newItem) {
            return false;
        }
    };

    @NonNull
    @Override
    public AccountHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.sample_account, parent, false);
        return new AccountHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountHolder holder, final int position) {
        final Account currentAccount = getItem(position);
        holder.tvAccountName.setText(currentAccount.getAccName());
        holder.tvBalance.setText(currency + currentAccount.getAccBalance());
        if (currentAccount.isSelected()) {
            holder.imDelete.setAlpha(0.2f);
            holder.imDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(context, R.string.account_in_use, Toast.LENGTH_LONG).show();
                }
            });
        } else
            holder.imDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    delete(currentAccount, position);
                }
            });
        holder.imEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                edit(currentAccount);
            }
        });
        holder.imInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                info(currentAccount);
            }
        });
        holder.imSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                select(currentAccount);
            }
        });
        if (currentAccount.isSelected())
            holder.tvInUse.setVisibility(View.VISIBLE);
        else
            holder.tvInUse.setVisibility(View.GONE);
    }

    private void delete(final Account account, final int position) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.confirm)
                .setMessage(R.string.delete_acc_confirm)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        accountsViewModel.delete(account);
                        notifyItemRemoved(position);
                    }
                })
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void edit(Account account) {
        Intent intent = new Intent(context, NewAccount.class);
        intent.putExtra("isNewAccount", false);
        intent.putExtra("updatingAccount", account);
        context.startActivity(intent);
    }

    private void info(Account account) {
        Intent intent = new Intent(context, AccountDetails.class);
        intent.putExtra("neededAccount", account);
        context.startActivity(intent);
    }

    private void select(Account account) {
        account.setSelected(true);
        accountsViewModel.update(account);
        List<Account> allAccounts = accountsViewModel.getAllAccounts();
        for (int i = 0; i < allAccounts.size(); i++) {
            if (allAccounts.get(i).getId() != account.getId()) {
                allAccounts.get(i).setSelected(false);
                accountsViewModel.update(allAccounts.get(i));
            }
        }
    }

    class AccountHolder extends RecyclerView.ViewHolder {
        private TextView tvAccountName;
        private TextView tvBalance;
        private TextView tvInUse;
        private ImageButton imDelete;
        private ImageButton imEdit;
        private ImageButton imInfo;
        private ImageButton imSelect;

        public AccountHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.accountName);
            tvBalance = itemView.findViewById(R.id.balance);
            tvInUse = itemView.findViewById(R.id.inUse);
            imDelete = itemView.findViewById(R.id.delete);
            imEdit = itemView.findViewById(R.id.edit);
            imInfo = itemView.findViewById(R.id.info);
            imSelect = itemView.findViewById(R.id.select);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View hiddenPart = v.findViewById(R.id.accActions);
                    if (hiddenPart.getVisibility() == View.GONE)
                        hiddenPart.setVisibility(View.VISIBLE);
                    else
                        hiddenPart.setVisibility(View.GONE);
                    notifyItemChanged(getAdapterPosition());
                }
            });
        }
    }
}
