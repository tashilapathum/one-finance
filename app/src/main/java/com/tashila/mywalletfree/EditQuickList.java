package com.tashila.mywalletfree;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditQuickList extends AppCompatActivity {
    SharedPreferences sharedPref;
    public static final String TAG = "EditQuickList";
    Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toolbar toolbar;
        sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);
        String theme = sharedPref.getString("theme", "light");
        if (theme.equalsIgnoreCase("dark")) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_quick_list);
            View layout = findViewById(R.id.rootLayout);
            layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_dark));
            toolbar = findViewById(R.id.toolbar);
            toolbar.setBackground(getDrawable(R.color.colorToolbarDark));
        }
        else {
            setTheme(R.style.AppTheme);
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_quick_list);
            toolbar = findViewById(R.id.toolbar);
        }

        //toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EditQuickList.this, Settings.class);
                startActivity(intent);
            }
        });

        //disable editing
        EditText quickList = findViewById(R.id.quickList);
        quickList.setEnabled(false);

        if (sharedPref.getInt("showed", 0) == 0)
            showInstructions();

        //load list
        String fullQuickListStr = sharedPref.getString("fullQuickListStr", "");
        if (!fullQuickListStr.isEmpty()) {
            String[] fullQuickList = fullQuickListStr.split("~");
            for (int i = 0; i < fullQuickList.length; i++)
                quickList.append(fullQuickList[i] + "\n");
        }
    }

    @Override //so the language change works with dark mode
    public void applyOverrideConfiguration(Configuration overrideConfiguration) {
        if (overrideConfiguration != null) {
            int uiMode = overrideConfiguration.uiMode;
            overrideConfiguration.setTo(getBaseContext().getResources().getConfiguration());
            overrideConfiguration.uiMode = uiMode;
        }
        super.applyOverrideConfiguration(overrideConfiguration);
    }

    public void showInstructions() {
        new AlertDialog.Builder(this)
                .setTitle("Instructions")
                .setMessage(R.string.quick_list_instructions)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sharedPref.edit().putInt("showed", 1).apply();
                    }
                }).show();
    }

    public void onClickEdit(View view) {
        //register clicks
        int clickCount = sharedPref.getInt("clickCount", 0);
        clickCount += 1;
        sharedPref.edit().putInt("clickCount", clickCount).apply();

        //edit
        EditText quickList = findViewById(R.id.quickList);
        quickList.setEnabled(true);
        quickList.requestFocus();

        button = findViewById(R.id.button);
        button.setText(getResources().getString(R.string.save));

        //save
        if (sharedPref.getInt("clickCount", 0) == 2) {
            saveQuickList();
            sharedPref.edit().putInt("clickCount", 0).apply();
        }
    }

    public void saveQuickList() {
        EditText quickList = findViewById(R.id.quickList);
        int lineCount = quickList.getLineCount();

        /**Free version
         Remove "if" part in the pro**/

        if (lineCount > 10)
            Toast.makeText(this, "Get the Pro version to add more than 5 items!", Toast.LENGTH_LONG).show();

        else {
            /*-------verify-------*/
            //no currency
            String currency = sharedPref.getString("currency", "");
            boolean hasCurrency = false;
            if (!currency.equals("") && quickList.getText().toString().contains(currency))
                hasCurrency = true;

            //empty
            boolean empty = false;
            if (quickList.getText().toString().isEmpty()) empty = true;


            if (empty || lineCount % 2 == 0 && !hasCurrency) {
                quickList.setEnabled(false);

                //save
                String fullQuickListStr = quickList.getText().toString();
                sharedPref.edit().putString("fullQuickListStr", fullQuickListStr).apply();

                //show
                String[] fullQuickList = fullQuickListStr.split("\n");
                if (!quickList.getText().toString().isEmpty()) {
                    for (int i = 0; i < fullQuickList.length; i++)
                        if (!fullQuickList[i].isEmpty() || !fullQuickList[i].equals("\n"))
                            quickList.append(fullQuickList[i] + "\n");
                }
                //fix blank lines
                if (quickList.getText().toString().contains("\n\n"))
                    quickList.setText(quickList.getText().toString().replace("\n\n", "\n"));

                button.setText(getResources().getString(R.string.edit));
                Toast.makeText(this, R.string.restart_for_changes, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.entered_incorrectly, Toast.LENGTH_LONG).show();
                showInstructions();
            }
        }
    }

}




















