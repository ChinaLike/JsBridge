package com.like.jsbridge

import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.core.web.JsBridgeWebView
import com.gyf.immersionbar.ImmersionBar

class WebActivity : AppCompatActivity() {

    private val webView: CustomWebView by lazy {
        findViewById(R.id.webView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).statusBarDarkFont(false).barColor(android.R.color.white).init()
        setContentView(R.layout.activity_web)

        webView.addJavascriptInterface(JsBridgeToast(this))
        webView.addJavascriptInterface(JsBridgeDialog(this))
        webView.addJavascriptInterface(ConstantJsBridge(this), webView.jsCallSynchronizeCallbackName())

        webView.loadUrl("")
    }
}