package com.tantalum.financejournal;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        String activityTitle = getIntent().getStringExtra("title");
        String url = getIntent().getStringExtra("url");

        ((TextView) findViewById(R.id.activityTitle)).setText(activityTitle);

        WebView webView = findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(url);
    }

    public void onClickCancel(View view) {
        finish();
    }
}