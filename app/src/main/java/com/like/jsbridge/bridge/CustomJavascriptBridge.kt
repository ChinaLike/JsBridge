package com.like.jsbridge.bridge

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.core.bridge.annotation.JavascriptBridge
import com.core.bridge.callback.Callback
import com.like.jsbridge.CustomBean

/**
 * js调用原生方法
 * @author like
 * @date 2024/2/8 14:58
 */
@JavascriptBridge("custom")
class CustomJavascriptBridge(private val context: Context) {
    @JavascriptInterface
    fun nativeNoArgAndNoCallback() {
        Toast.makeText(context, "调用原生无参数无回调方法", Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun nativeNoArgAndCallback(callback: Callback) {
        callback.success("成功回调")
    }

    @JavascriptInterface
    fun nativeArgAndNoCallback(params: String) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun nativeArgAndCallback(params: String, callback: Callback) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show()
        callback.success("成功回调")
    }

    @JavascriptInterface
    fun nativeDeleteCallback(params: String, callback: Callback) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show()
        callback.success(true)
        Handler().postDelayed(Runnable {
            callback.callback(1, "失败", null)
        }, 3000)
    }

    @JavascriptInterface
    fun nativeNoDeleteCallback(data: String, callback: Callback) {
        Toast.makeText(context, data, Toast.LENGTH_SHORT).show()
        callback.success(false)
        Handler().postDelayed(Runnable {
            callback.callback(1, "失败", null)
        }, 3000)
    }

    @JavascriptInterface
    fun nativeSyncCallback(): String {
        return "原生同步回调"
    }

    @JavascriptInterface
    fun nativeDialog(params: String?, successCallback: Callback, failCallback: Callback) {
        val bean = JSON.parseObject(params, CustomBean::class.java)
        AlertDialog.Builder(context)
            .setTitle(bean.title)
            .setMessage(bean.content)
            .setPositiveButton(
                bean.confirmText,
                DialogInterface.OnClickListener { dialog, which -> successCallback.success("我同意了") })
            .setNegativeButton(
                bean.cancelText,
                DialogInterface.OnClickListener { dialog, which ->
                    failCallback.callback(
                        2,
                        "我不同意",
                        ""
                    )
                })
            .show()
    }

    @JavascriptInterface
    fun statusHeight(): Int {
        var result = 0
        val resourceId = context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return px2dp(result.toFloat())
    }

    @JavascriptInterface
    fun testBoolean(success:Boolean){

    }

    fun px2dp(pxValue: Float): Int {
        return (pxValue / Resources.getSystem().displayMetrics.density + 0.5F).toInt()
    }
}