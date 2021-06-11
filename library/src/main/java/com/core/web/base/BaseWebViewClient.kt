package com.core.web.base

import android.util.Log
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

    /**
     * 已经注入的Url
     */
    private val injectUrl = mutableListOf<String>()

    @CallSuper
    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
        if (view is BaseWebView && !injectUrl.contains(url)) {
            if (jsInject == null) {
                jsInject = JsInject(view)
            }
            view.loadUrl("javascript:" + jsInject!!.injectJs())
            if (BuildConfig.DEBUG) {
                Log.d("注入的数据", jsInject!!.injectJs())
            }
            injectUrl.add(url)
        }
    }

}