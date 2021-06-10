package com.core.web

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alibaba.fastjson.JSON

/**
 * 自定义WebView
 * @author like
 * @date 5/24/21 4:19 PM
 */
open class BaseWebView : WebView, IWebView {

    /**
     * js回调管理
     */
    val callbackManager: MutableMap<String, JsCallback> = mutableMapOf()

    /**
     * 存放注入的对象
     */
    val javascriptInterfaceList: MutableList<JavascriptInterfaceBean> = mutableListOf()

    /**
     * 内部使用注入名称
     */
    val innerJavascriptInterfaceName: String = "JsWebViewBridge"

    private var uniqueId = 0

    constructor(context: Context) : super(context) {
        initWebView()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initWebView()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initWebView()
    }

    private fun initWebView() {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
        webViewClient = BaseWebViewClient()
        webChromeClient = WebChromeClient()
        addJavascriptInterface(BaseJavascriptInterface(this), innerJavascriptInterfaceName)
    }

    /**
     * 回调
     * @param [cbId] 回调的Id
     * @param [data] 回调的数据
     * @param [isDeleteId] 是否删除当前回调，删除后后面的回调将不会执行
     */
    override fun callback(cbId: String, data: CallbackBean, isDeleteId: Boolean) {
        post {
            try {
                val resultData: String = JSON.toJSONString(data)
                val javascriptString = "${jsCallName()}.on('${cbId}','${resultData}',${isDeleteId})"
                loadUrl("javascript:$javascriptString")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [callback] 回调
     */
    override fun callJsFunction(function: String, callback: JsCallback?) {
        callJsFunction(function, null, callback)
    }

    /**
     * 调用Js代码
     * @param [function] js的方法
     */
    override fun callJsFunction(function: String) {
        callJsFunction(function, null, null)
    }

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [data] 传递给js的数据
     * @param [callback] 回调
     */
    override fun callJsFunction(function: String, data: String?, callback: JsCallback?) {
        post {
            try {
                var callbackJsStr: String? = null
                if (callback != null) {
                    uniqueId++
                    var cbIdStr = "${function}${System.currentTimeMillis()}${uniqueId}"
                    callbackManager[cbIdStr] = callback
                    val jsInject = JsInject(this)
                    callbackJsStr = jsInject.callbackJsInject(cbIdStr)
                }
                var loadJsStr: String? = null
                if (data != null && callbackJsStr != null) {
                    loadJsStr = "${function}('${data}',${callbackJsStr})"
                } else if (data != null && callbackJsStr == null) {
                    loadJsStr = "${function}('${data}')"
                } else if (data == null && callbackJsStr != null) {
                    loadJsStr = "${function}(${callbackJsStr})"
                } else if (data == null && callbackJsStr == null) {
                    loadJsStr = "${function}()"
                }
                loadJsStr?.let { loadUrl("javascript:$loadJsStr") }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [data] 传递给js的数据
     */
    override fun callJsFunction(function: String, data: String?) {
        callJsFunction(function, data, null)
    }

    @SuppressLint("JavascriptInterface")
    override fun addJavascriptInterface(`object`: Any, name: String) {
        if (name != "MiWebViewDetector" && name != innerJavascriptInterfaceName) {
            javascriptInterfaceList.add(JavascriptInterfaceBean(`object`, name))
        } else {
            super.addJavascriptInterface(`object`, name)
        }
    }

    /**
     * 添加需要注入的类
     */
    fun addJavascriptInterface(`object`: Any) {
        addJavascriptInterface(`object`, "")
    }

    override fun setWebViewClient(client: WebViewClient) {
        if (client is BaseWebViewClient) {
            super.setWebViewClient(client)
        } else {
            throw IllegalArgumentException("请传入继承BaseWebViewClient的WebViewClient")
        }
    }

}