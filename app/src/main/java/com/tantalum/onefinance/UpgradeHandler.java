package com.tantalum.onefinance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class UpgradeHandler {
    public static final String ONE_FINANCE_PRO = "one_finance_pro";

    public static boolean isProActive(Context context) {
        SharedPreferences pref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        boolean isPro = pref.getBoolean(ONE_FINANCE_PRO, false);
        return isPro;
    }

    public static void activatePro(Context context) {
        SharedPreferences pref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);
        pref.edit().putBoolean(ONE_FINANCE_PRO, true).apply();

        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.p_success)
                .setMessage(R.string.thank_u_for_pro)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context, MainActivity.class));
                    }
                })
                .setCancelable(false)
                .show();
    }
}
