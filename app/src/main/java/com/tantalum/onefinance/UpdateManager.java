package com.tantalum.onefinance;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;

public class UpdateManager {
    private final Context context;
    private final FirebaseFirestore firestore;
    private final Handler handler;

    public UpdateManager(Context context) {
        this.context = context;
        firestore = FirebaseFirestore.getInstance();
        handler = new Handler(Looper.getMainLooper());
    }

    public void checkForUpdates() {
        if (shouldCheck())
            handler.postDelayed(() ->
                    firestore.collection("updates")
                            .document("update")
                            .get()
                            .addOnSuccessListener(documentSnapshot -> {
                                Update update = documentSnapshot.toObject(Update.class);
                                if (update.getVersion() > BuildConfig.VERSION_CODE) {
                                    new MaterialAlertDialogBuilder(context)
                                            .setTitle("Update available")
                                            .setMessage("An update for " + context.getString(R.string.app_name)
                                                    + " is available. Update the app for the "
                                                    + "latest features, performance improvements and bug fixes.")
                                            .setPositiveButton("Update", (dialogInterface, i) -> {
                                                String url = update.getUrl();
                                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                                context.startActivity(intent);
                                                if (update.getMandatory())
                                                    ((MainActivity) context).finish();
                                            })
                                            .setNegativeButton("Cancel", (dialogInterface, i) -> {
                                                if (update.getMandatory())
                                                    ((MainActivity) context).finish();
                                            })
                                            .setCancelable(false)
                                            .show();
                                }
                            }), 3000);

    }

    /**Only check every 5 days*/
    private boolean shouldCheck() {
        int day = LocalDate.now().getDayOfMonth();
        return day % 5 == 0;
    }

}
