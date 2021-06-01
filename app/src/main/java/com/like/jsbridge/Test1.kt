package com.like.jsbridge

import android.util.Log
import android.webkit.JavascriptInterface
import com.core.web.Callback

/**
 *
 * @author like
 * @date 5/26/21 4:43 PM
 */
class Test1 {

    @JavascriptInterface
    fun toast1(params: Int,callback: Callback) {
        Log.d("测试", "toast1=${params}")
        callback.success()
        callback.error(1,"123")
    }

    @JavascriptInterface
    fun test2(params: String) {
        Log.d("测试", "test2")
    }

    @JavascriptInterface
    fun test3(params: Int, callback: Callback) {
        Log.d("测试", "test3")
    }

}