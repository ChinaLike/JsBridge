package com.like.jsbridge;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.core.web.Callback;
import com.core.web.base.IJavascriptInterface;

/**
 * @author like
 * @date 6/11/21 5:31 PM
 */
class JsBridgeToastJava implements IJavascriptInterface {

    private Context context;

    public JsBridgeToastJava(Context context){
        this.context = context;
    }

    @JavascriptInterface
    public void nativeNoArgAndNoCallback(){
        Toast.makeText(context,"调用原生无参数无回调方法",Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void nativeNoArgAndCallback(Callback callback){
        callback.success();
    }

    @JavascriptInterface
    public void nativeArgAndNoCallback(String params){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void nativeArgAndCallback(String params,Callback callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
        callback.success();
    }

    @JavascriptInterface
    public void nativeDeleteCallback(String params,Callback callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
        callback.success(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.error(1,"错误回调");
            }
        },3000);
    }

    @JavascriptInterface
    public void nativeNoDeleteCallback(String params,Callback callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
        callback.success(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.error(1,"错误回调");
            }
        },3000);
    }

    @JavascriptInterface
    public String nativeSyncCallback(){
        return "原生同步回调";
    }

}
