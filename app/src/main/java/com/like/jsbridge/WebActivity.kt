package com.like.jsbridge

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.core.web.JsBridgeWebView
import com.gyf.immersionbar.ImmersionBar
import com.like.jsbridge.bridge.CustomJavascriptBridge

class WebActivity : AppCompatActivity() {

    private val webView: JsBridgeWebView by lazy {
        findViewById(R.id.webView)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ImmersionBar.with(this).statusBarDarkFont(false).barColor(android.R.color.white).init()
        setContentView(R.layout.activity_web)
        webView.setGroup("custom")
        webView.addJavascriptInterface(CustomJavascriptBridge(this))
        webView.loadUrl("https://github.com/ChinaLike/JsBridge")
    }
}