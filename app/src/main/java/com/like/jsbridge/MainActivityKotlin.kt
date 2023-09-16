package com.like.jsbridge

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.core.web.JsBridgeWebView
import com.core.web.JsCallback

class MainActivityKotlin : AppCompatActivity() {
    private var webView: JsBridgeWebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        webView?.addJavascriptInterface(JsBridgeToast(this))
        webView?.addJavascriptInterface(JsBridgeDialog(this))
        webView?.addJavascriptInterface(ConstantJsBridge(this),webView?.jsCallName() + "Constant")
        webView?.loadUrl("file:///android_asset/test.html")

        //调用Js无参数无回调
        findViewById<View>(R.id.jsNoArgAndNoCallback).setOnClickListener { v: View? ->
            webView?.callJsFunction("jsNoArgAndNoCallback")
        }

        //调用Js无参数有回调
        findViewById<View>(R.id.jsNoArgAndCallback).setOnClickListener { v: View? ->
            webView?.callJsFunction("jsNoArgAndCallback", object : JsCallback {
                override fun onCallback(callback: Any) {
                    Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
                }
            })
        }

        //调用Js有参数无回调
        findViewById<View>(R.id.jsArgAndNoCallback).setOnClickListener { v: View? ->
            webView?.callJsFunction("jsArgAndNoCallback","原生传递过来的参数")
        }

        //调用Js有参数有回调（可回调多次）
        findViewById<View>(R.id.jsArgAndCallback).setOnClickListener { v: View? ->
            webView?.callJsFunction("jsArgAndCallback","原生传递过来的参数", object : JsCallback {
                override fun onCallback(callback: Any) {
                    Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
                }
            })
        }

        //调用Js有参数有回调（只能回调一次）
        findViewById<View>(R.id.jsArgAndDeleteCallback).setOnClickListener { v: View? ->
            webView?.callJsFunction("jsArgAndDeleteCallback","原生传递过来的参数", object : JsCallback {
                override fun onCallback(callback: Any) {
                    Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }
}