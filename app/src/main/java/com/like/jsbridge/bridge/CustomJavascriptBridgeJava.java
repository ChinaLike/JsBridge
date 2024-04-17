package com.like.jsbridge.bridge;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Handler;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.core.bridge.annotation.JavascriptBridge;
import com.core.bridge.callback.Callback;
import com.like.jsbridge.CustomBean;

/**
 * @author like
 * @date 2024/2/8 17:40
 */
@JavascriptBridge(group = "customJava")
public class CustomJavascriptBridgeJava {

    private Context context;

    public CustomJavascriptBridgeJava(Context context) {
        this.context = context;
    }

    @JavascriptInterface
    public void nativeNoArgAndNoCallback() {
        Toast.makeText(context, "调用原生无参数无回调方法", Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void nativeNoArgAndCallback(Callback callback) {
        callback.success("成功回调");
    }

    @JavascriptInterface
    public void nativeArgAndNoCallback(String params) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show();
    }

    @JavascriptInterface
    public void nativeArgAndCallback(String params, Callback callback) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show();
        callback.success("成功回调");
    }

    @JavascriptInterface
    public void nativeDeleteCallback(String params, Callback callback) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show();
        callback.success(true);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.callback(1, "错误回调", "");
            }
        }, 3000);
    }

    @JavascriptInterface
    public void nativeNoDeleteCallback(String params, Callback callback) {
        Toast.makeText(context, params, Toast.LENGTH_SHORT).show();
        callback.success(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                callback.callback(1, "错误回调", "");
            }
        }, 3000);
    }

    @JavascriptInterface
    public String nativeSyncCallback() {
        return "原生同步回调";
    }

    @JavascriptInterface
    public void nativeDialog(String params,Callback successCallback,Callback failCallback){
        CustomBean bean = JSONObject.parseObject(params,CustomBean.class);
        new AlertDialog.Builder(context)
                .setTitle(bean.getTitle())
                .setMessage(bean.getContent())
                .setPositiveButton(bean.getConfirmText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        successCallback.success("我同意了");
                    }
                })
                .setNegativeButton(bean.getCancelText(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        failCallback.callback(2,"我不同意","");
                    }
                })
                .show();
    }

    @JavascriptInterface
    public int statusHeight() {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return px2dp(result);
    }

    private int px2dp(float pxValue) {
        return (int) (pxValue / Resources.getSystem().getDisplayMetrics().density + 0.5F);
    }
}
