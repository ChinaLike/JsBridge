package com.core.web.base

import android.graphics.Bitmap
import android.util.Log
import android.webkit.ValueCallback
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper
import com.core.web.BuildConfig
import com.core.web.JsInject

/**
 *  自定义WebViewClient客户端，如果需要重写该类需要继承改类
 * @author like
 * @date 5/25/21 11:31 AM
 */
open class BaseWebViewClient : WebViewClient() {

    private var jsInject: JsInject? = null

    @CallSuper
    override fun onPageStarted(view: WebView?, url: String, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        (view as? BaseWebView)?.let { inject(it, url) }
    }

    @CallSuper
    override fun onLoadResource(view: WebView?, url: String) {
        super.onLoadResource(view, url)
        (view as? BaseWebView)?.let { inject(it, url) }
    }

    @CallSuper
    override fun onPageCommitVisible(view: WebView?, url: String) {
        super.onPageCommitVisible(view, url)
        (view as? BaseWebView)?.let { inject(it, url) }
    }

    @CallSuper
    override fun onPageFinished(view: WebView?, url: String) {
        super.onPageFinished(view, url)
        (view as? BaseWebView)?.let { inject(it, url) }
    }

    /**
     * 注入Js
     */
    private fun inject(webView: BaseWebView, url: String) {
        webView.isInjectSuccess {
            if (!it) {
                if (jsInject == null) {
                    jsInject = JsInject(webView)
                }
                webView.evaluateJavascript("javascript:" + jsInject!!.injectJs(), ValueCallback { })
                if (BuildConfig.DEBUG) {
                    Log.d("注入的数据", jsInject!!.injectJs())
                }
            }
        }
    }

}