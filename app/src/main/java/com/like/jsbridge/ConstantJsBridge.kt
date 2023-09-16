package com.like.jsbridge

import android.content.Context
import android.webkit.JavascriptInterface

/**
 *
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
        return result
    }

}