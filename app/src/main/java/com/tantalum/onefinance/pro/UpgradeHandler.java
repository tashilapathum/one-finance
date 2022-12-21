package com.tantalum.onefinance.pro;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.tantalum.onefinance.MainActivity;
import com.tantalum.onefinance.R;

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

    public static void showPrompt(Context context) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Remove ads?")
                .setMessage(
                        "Upgrade to pro version for a small one-time fee, to remove ads forever plus many more extended features!"
                                + "\n\n"
                                + context.getString(R.string.pro_features)
                )
                .setPositiveButton(R.string.upgrade_to_pro, (dialog, which) -> {
                    context.startActivity(new Intent(context, UpgradeToProActivity.class));
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
