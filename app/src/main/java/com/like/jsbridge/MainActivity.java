package com.like.jsbridge;

import android.os.Bundle;
import android.widget.Toast;

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

        //调用Js无参数无回调
        findViewById(R.id.jsNoArgAndNoCallback).setOnClickListener(v ->
                webView.callJsFunction("jsNoArgAndNoCallback")
                );



//        findViewById(R.id.jsNoArgAndNoCallback).setOnClickListener(v ->
//                webView.callJsFunction("jsNoArgAndNoCallback", "342s", callback -> Toast.makeText(MainActivity.this, "" + callback, Toast.LENGTH_SHORT).show()));
    }
}