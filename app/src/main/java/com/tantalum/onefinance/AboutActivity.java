package com.tantalum.onefinance;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;

import java.util.Locale;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPref = getSharedPreferences("myPref", MODE_PRIVATE);

        //language
        String language = sharedPref.getString("language", "english");
        if (language.equals("සිංහල")) {
            Locale locale = new Locale("si");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.setLocale(locale);
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        //theme
        String theme = sharedPref.getString("theme", "light");
        if (!theme.equalsIgnoreCase("dark"))
            setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void rateApp(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.tantalum.onefinance"));
        startActivity(intent);
    }

    public void moreApps(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Tashila+Pathum"));
        startActivity(intent);
    }

    public void showLicenses(View view) {
        startActivity(new Intent(this, OssLicensesMenuActivity.class));
    }

    public void openPrivacyPolicy(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tashila.me/projects/one-finance/privacy"));
        startActivity(intent);
    }

    public void openTC(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tashila.me/projects/one-finance/terms"));
        startActivity(intent);
    }

    public void showChangelog(View view) {
        DialogWhatsNew dialogWhatsNew = new DialogWhatsNew();
        dialogWhatsNew.show(getSupportFragmentManager(), "changelog dialog");
    }

    public void onClickBack(View view) {
        finish();
    }
}