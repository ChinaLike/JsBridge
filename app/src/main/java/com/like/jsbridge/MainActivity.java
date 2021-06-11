package com.like.jsbridge;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.core.web.JsBridgeWebView;

public class MainActivity extends AppCompatActivity {

    private JsBridgeWebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);

        webView.addJavascriptInterface(new JsBridgeToast(this));
        webView.addJavascriptInterface(new JsBridgeDialog(this));

        webView.loadUrl("file:///android_asset/test.html");

        //调用Js无参数无回调
        findViewById(R.id.jsNoArgAndNoCallback).setOnClickListener(v ->
                webView.callJsFunction("jsNoArgAndNoCallback")
        );

        //调用Js无参数有回调
        findViewById(R.id.jsNoArgAndCallback).setOnClickListener(v ->
                webView.callJsFunction("jsNoArgAndCallback", callback -> Toast.makeText(MainActivity.this, "" + callback, Toast.LENGTH_SHORT).show())
        );

        //调用Js有参数无回调
        findViewById(R.id.jsArgAndNoCallback).setOnClickListener(v ->
                webView.callJsFunction("jsArgAndNoCallback", "原生传递过来的参数")
        );

        //调用Js有参数有回调（可回调多次）
        findViewById(R.id.jsArgAndCallback).setOnClickListener(v ->
                webView.callJsFunction("jsArgAndCallback", "原生传递过来的参数", callback -> Toast.makeText(MainActivity.this, "" + callback, Toast.LENGTH_SHORT).show())

        );

        //调用Js有参数有回调（只能回调一次）
        findViewById(R.id.jsArgAndDeleteCallback).setOnClickListener(v ->
                webView.callJsFunction("jsArgAndDeleteCallback", "原生传递过来的参数", callback -> Toast.makeText(MainActivity.this, "" + callback, Toast.LENGTH_SHORT).show())

        );



    }
}