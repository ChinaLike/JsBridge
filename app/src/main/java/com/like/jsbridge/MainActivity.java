package com.like.jsbridge;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private DemoWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        webView.getSettings().setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new JsBridgeToast(this));
        webView.addJavascriptInterface(new JsBridgeDialog(this));

        webView.loadUrl("file:///android_asset/test.html");

    }
}