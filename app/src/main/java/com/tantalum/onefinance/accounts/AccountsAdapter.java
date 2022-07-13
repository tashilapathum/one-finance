package com.tantalum.onefinance.accounts;

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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.onefinance.R;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

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
        final boolean[] expanded = {false};
        holder.tvAccountName.setText(currentAccount.getAccName());
        holder.tvBalance.setText(currency + currentAccount.getAccBalance());
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
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View hiddenPart = v.findViewById(R.id.accActions);
                if (hiddenPart.getVisibility() == View.VISIBLE)
                    hiddenPart.setVisibility(View.GONE);
                else
                    hiddenPart.setVisibility(View.VISIBLE);
                holder.itemView.invalidate();
                notifyItemChanged(position);
            }
        });
    }

    private void delete(final Account account, final int position) {
        new MaterialAlertDialogBuilder(context)
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
        intent.putExtra("neededAccountName", account.getAccName());
        context.startActivity(intent);
    }

    private void info(Account account) {
        Intent intent = new Intent(context, AccountDetails.class);
        intent.putExtra("neededAccountName", account.getAccName());
        context.startActivity(intent);
    }

    class AccountHolder extends RecyclerView.ViewHolder {
        private TextView tvAccountName;
        private TextView tvBalance;
        private ImageButton imDelete;
        private ImageButton imEdit;
        private ImageButton imInfo;

        public AccountHolder(@NonNull View itemView) {
            super(itemView);
            tvAccountName = itemView.findViewById(R.id.accountName);
            tvBalance = itemView.findViewById(R.id.balance);
            imDelete = itemView.findViewById(R.id.delete);
            imEdit = itemView.findViewById(R.id.edit);
            imInfo = itemView.findViewById(R.id.info);
        }
    }
}
