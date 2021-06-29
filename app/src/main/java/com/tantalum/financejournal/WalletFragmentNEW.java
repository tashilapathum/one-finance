package com.tantalum.financejournal;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

public class WalletFragmentNEW extends Fragment {
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_wallet, container, false);

        SpeedDialView fab = view.findViewById(R.id.fab);
        fab.inflate(R.menu.wallet_fab_menu);
        fab.setOnActionSelectedListener(new SpeedDialView.OnActionSelectedListener() {
            @Override
            public boolean onActionSelected(SpeedDialActionItem actionItem) {
                int transactionType = 0;
                switch (actionItem.getId()) {
                    case R.id.add_expense: {
                        transactionType = Constants.EXPENSE;
                        break;
                    }
                    case R.id.add_income: {
                        transactionType = Constants.INCOME;
                        break;
                    }
                    case R.id.add_transfer: {
                        transactionType = Constants.TRANSFER;
                        break;
                    }
                }
                new DialogWalletInput(transactionType).show(getChildFragmentManager(), "wallet input dialog");
                fab.close();
                return false;
            }
        });

        return view;
    }
}
