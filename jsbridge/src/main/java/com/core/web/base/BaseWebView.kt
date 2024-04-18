package com.core.web.base

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import com.alibaba.fastjson.JSON
import com.core.bridge.annotation.JavascriptBridge
import com.core.bridge.callback.CallbackBean
import com.core.bridge.callback.ICallback
import com.core.bridge.callback.JsCallbackNative
import com.core.bridge.interfaces.IInjectBridge
import com.core.util.ClassUtils
import com.core.web.*

/**
 * 自定义WebView,使用时如果没有特殊要求尽量不要重写该类，使用[JsBridgeWebView]类即可
 * @author like
 * @date 5/24/21 4:19 PM
 */
abstract class BaseWebView(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    WebView(context, attrs, defStyle), IWebView, ICallback, IInjectBridge {

    private var mWebViewClient:BaseWebViewClient? = null

    private var uniqueId = 0

    /**
     * 组
     */
    private var group: String? = null

    /**
     * 用户注入的桥梁类
     */
    private val userInjectBridgeMap: MutableMap<String, Any> = mutableMapOf()

    /**
     * 保存Js回调给原生数据
     */
    private val javascriptCallbackMap: MutableMap<String, JsCallbackNative> = mutableMapOf()

    /**
     * 注入的类
     */
    private val jsInject: JsInject by lazy {
        JsInject(this)
    }

    init {
        initWebView()
    }

    override fun loadData(data: String, mimeType: String?, encoding: String?) {
        mWebViewClient?.setInject(jsInject)
        super.loadData(data, mimeType, encoding)
    }

    override fun loadDataWithBaseURL(
        baseUrl: String?,
        data: String,
        mimeType: String?,
        encoding: String?,
        historyUrl: String?
    ) {
        mWebViewClient?.setInject(jsInject)
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
    }

    override fun loadUrl(url: String) {
        mWebViewClient?.setInject(jsInject)
        super.loadUrl(url)
    }

    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) {
        mWebViewClient?.setInject(jsInject)
        super.loadUrl(url, additionalHttpHeaders)
    }
    private fun initWebView() {
        settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setWebContentsDebuggingEnabled(true)
        }
        webViewClient = BaseWebViewClient()
        webChromeClient = WebChromeClient()
    }

    override fun callback(cbId: String, data: CallbackBean, deleteCb: Boolean) {
        post {
            try {
                val resultData: String = JSON.toJSONString(data)
                val javascriptString = "${jsCallName()}.on('${cbId}','${resultData}',${deleteCb})"
                loadUrl("javascript:$javascriptString")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * 获取Js回调给原生的接口
     * @param [cbId] 回调Id
     * @param [deleteCbId] 是否删除回调Id
     */
    override fun getJavascriptCallbackNativeId(
        cbId: String,
        deleteCbId: Boolean
    ): JsCallbackNative? {
        val jsCallbackNative = javascriptCallbackMap[cbId]
        if (jsCallbackNative != null && deleteCbId) {
            javascriptCallbackMap.remove(cbId)
        }
        return jsCallbackNative
    }

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [callback] 回调
     */
    override fun callJsFunction(function: String, callback: JsCallbackNative?) {
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
    override fun callJsFunction(function: String, data: String?, callback: JsCallbackNative?) {
        post {
            try {
                var callbackJsStr: String? = null
                if (callback != null) {
                    uniqueId++
                    var cbIdStr = "${function}${System.currentTimeMillis()}${uniqueId}"
                    javascriptCallbackMap[cbIdStr] = callback
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
        super.addJavascriptInterface(`object`, name)
    }

    /**
     * 设置组
     */
    final override fun setGroup(group: String) {
        this.group = group
        val clazzName = ClassUtils.getBridgeByGroup(group)
        if (!clazzName.isNullOrEmpty()) {
            val clazz = Class.forName(clazzName)
            val constructor = clazz.getConstructor(ICallback::class.java, IInjectBridge::class.java)
            val instance = constructor.newInstance(this, this)
            addJavascriptInterface(instance, jsCallName())
        }
    }

    /**
     * 获取组
     */
    final override fun getGroup(): String? {
        return group
    }

    /**
     * 添加需要注入的类
     */
    fun addJavascriptInterface(`object`: Any) {
        if (group.isNullOrEmpty()) {
            throw NullPointerException("请先调用setGroup(group)方法")
        }
        if (`object`.javaClass.isAnnotationPresent(JavascriptBridge::class.java)) {
            val annotation = `object`.javaClass.getAnnotation(JavascriptBridge::class.java)
            if (annotation.group == group) {
                userInjectBridgeMap[`object`.javaClass.name] = `object`
            } else {
                if (BuildConfig.DEBUG) {
                    throw RuntimeException("请添加与group一致的类，group=${group},添加类的group=${annotation.group}")
                }
            }
        } else {
            if (BuildConfig.DEBUG) {
                throw RuntimeException("${`object`.javaClass.name}未添加JavascriptBridge注解")
            }
        }
    }

    override fun setWebViewClient(client: WebViewClient) {
        if (client is BaseWebViewClient) {
            mWebViewClient = client
            super.setWebViewClient(client)
        } else {
            throw IllegalArgumentException("请传入继承BaseWebViewClient的WebViewClient")
        }
    }

    /**
     * 获取注入的桥梁类
     */
    override fun getInjectBridgeMap(): MutableMap<String, Any> {
        return userInjectBridgeMap
    }

}