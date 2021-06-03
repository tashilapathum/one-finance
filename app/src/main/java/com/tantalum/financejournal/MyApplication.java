package com.tantalum.financejournal;

import android.app.Application;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

public class MyApplication extends Application {
    private Thread.UncaughtExceptionHandler defaultUEH;

    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
            new AlertDialog.Builder(MyApplication.this)
                    .setTitle("An error occurred")
                    .setMessage("Something went wrong while performing the action.\nScare the developer by sending a bug report?")
                    .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MyApplication.this, "Report sent", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            defaultUEH.uncaughtException(t, e);
        }
    };

    public MyApplication() {
        defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
    }

}
