package com.like.jsbridge

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.webkit.JavascriptInterface

/**
 * 解决一些同步回调方法，不需要异步返回数据，这样调用方法就可以更早的调用，比如H5需要更早的获取状态栏高度，来适配界面
 * @author like
 * @date 2023/9/16 20:36
 */
class ConstantJsBridge(private val context: Context){

    @JavascriptInterface
    fun statusHeight():Int{
        var result = 0
        val resourceId = context.resources
            .getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return px2dp(result.toFloat())
    }

    fun px2dp(pxValue: Float): Int {
        return (pxValue / Resources.getSystem().displayMetrics.density + 0.5F).toInt()
    }

    @JavascriptInterface
    fun statusHeight(data:Int):Int{
        return data
    }

}