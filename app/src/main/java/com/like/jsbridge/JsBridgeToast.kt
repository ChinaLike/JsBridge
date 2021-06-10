package com.like.jsbridge

import android.content.Context
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.core.web.Callback
import com.core.web.CallbackBean

/**
 * 测试类
 * @author like
 * @date 5/26/21 4:43 PM
 */
class JsBridgeToast(private val context: Context)  {

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
    fun nativeArgAndCallback(params:String,callback: Callback):Boolean{
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success()

        return false
    }

    @JavascriptInterface
    fun nativeDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(isDelete = true)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }

    @JavascriptInterface
    fun nativeNoDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(isDelete = false)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }

}