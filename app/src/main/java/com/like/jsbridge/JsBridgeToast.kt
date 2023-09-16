package com.like.jsbridge

import android.content.Context
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.core.web.Callback
import com.core.web.CallbackBean
import com.core.web.base.IJavascriptInterface

/**
 * 测试类
 * @author like
 * @date 5/26/21 4:43 PM
 */
class JsBridgeToast(private val context: Context):IJavascriptInterface  {

    @JavascriptInterface
    fun nativeNoArgAndNoCallback(){
        Toast.makeText(context,"调用原生无参数无回调方法",Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun nativeNoArgAndCallback(callback: Callback){
        callback.success()
    }

    @JavascriptInterface
    fun nativeArgAndNoCallback(params:String){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
    }

    @JavascriptInterface
    fun nativeArgAndCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success()
    }

    @JavascriptInterface
    fun nativeDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(true)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }

    @JavascriptInterface
    fun nativeNoDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(false)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }

    @JavascriptInterface
    fun nativeSyncCallback():String{
        return "原生同步回调"
    }
}