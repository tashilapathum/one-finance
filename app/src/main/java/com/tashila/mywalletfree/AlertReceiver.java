package com.tashila.mywalletfree;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nb = notificationHelper.getChannel1Notification(notificationHelper.getString(R.string.notification_title),
                notificationHelper.getString(R.string.notification_msg));
        notificationHelper.getManager().notify(1, nb.build());

        //to update reports


        //calculate interests
        AccountHandler accountHandler = new AccountHandler(context);
        accountHandler.calculateInterests();
    }
}
