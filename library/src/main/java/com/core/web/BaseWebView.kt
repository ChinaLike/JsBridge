package com.core.web

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alibaba.fastjson.JSON

/**
 *
 * @author like
 * @date 5/24/21 4:19 PM
 */
abstract class BaseWebView : WebView {

    /**
     * 存放注入的对象
     */
    val javascriptInterfaceList: MutableList<JavascriptInterfaceBean> = mutableListOf()

    /**
     * 内部使用注入名称
     */
    val innerJavascriptInterfaceName:String = "JsWebViewBridge"

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
        settings.javaScriptEnabled = true
        webChromeClient = BaseWebChromeClient()
        webViewClient = BaseWebViewClient()

        addJavascriptInterface(BaseJavascriptInterface(this), innerJavascriptInterfaceName)
    }

    /**
     * Js调用原生的名字
     */
    open fun jsCallName(): String {
        return "JsBridge"
    }

    /**
     * 回调
     * @param [cbId] 回调的Id
     * @param [data] 回调的数据
     * @param [isDeleteId] 是否删除当前回调，删除后后面的回调将不会执行
     */
    @JvmOverloads
    fun callback(cbId: String, data: CallbackBean, isDeleteId: Boolean = false) {
        post {
            try {
                val resultData: String = JSON.toJSONString(data)
                val javascriptString ="${jsCallName()}.on('${cbId}','${resultData}',${isDeleteId})"
                loadUrl("javascript:$javascriptString")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    @SuppressLint("JavascriptInterface")
    override fun addJavascriptInterface(`object`: Any, name: String) {
        super.addJavascriptInterface(`object`, name)
        if (name != "MiWebViewDetector" && name != innerJavascriptInterfaceName) {
            javascriptInterfaceList.add(JavascriptInterfaceBean(`object`, name))
        }
    }

    override fun setWebChromeClient(client: WebChromeClient?) {
        if (client is BaseWebChromeClient) {
            super.setWebChromeClient(client)
        } else {
            throw IllegalArgumentException("请传入继承BaseWebChromeClient的WebChromeClient")
        }
    }

    override fun setWebViewClient(client: WebViewClient) {
        if (client is BaseWebViewClient) {
            super.setWebViewClient(client)
        } else {
            throw IllegalArgumentException("请传入继承BaseWebViewClient的WebViewClient¬")
        }
    }

    /**
     * 获取js的Window，可不用重写，如果遇到window对象不对可以使用这个重新赋值window
     */
    open fun getWindow():String{
        return "window"
    }

}