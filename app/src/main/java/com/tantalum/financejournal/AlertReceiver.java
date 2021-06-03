package com.tantalum.financejournal;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    public static final String TAG = "AlertReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        int id = (int) System.currentTimeMillis();
        SharedPreferences sharedPref = context.getSharedPreferences("myPref", Context.MODE_PRIVATE);

        //add transactions reminder
        NotificationCompat.Builder nb1 = notificationHelper.getChannel1Notification(
                notificationHelper.getString(R.string.trans_notify_title), notificationHelper.getString(R.string.notification_msg)
        );
        notificationHelper.getManager().notify(id, nb1.build());

        //due and overdue bills
        if (sharedPref.getBoolean("haveBills", false)) {
            NotificationCompat.Builder nb2 = notificationHelper.getChannel2Notification(
                    notificationHelper.getString(R.string.bills_notify_title), notificationHelper.getString(R.string.notification_msg)
            );
            notificationHelper.getManager().notify(id, nb2.build());
        }
    }
}
