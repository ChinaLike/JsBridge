# JsBridge
[![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html)  [![API](https://img.shields.io/badge/API-17%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=17)  [![](https://jitpack.io/v/ChinaLike/JsBridge.svg)](https://jitpack.io/#ChinaLike/JsBridge)  [![Gradle-4.1.2](https://img.shields.io/badge/Gradle-4.1.2-brightgreen.svg)](https://img.shields.io/badge/Gradle-4.1.2-brightgreen.svg)  [![](https://img.shields.io/badge/language-kotlin-brightgreen.svg)](https://kotlinlang.org/)

# SDK支持

+ Js调用原生方法并支持异步回调和同步回调
+ 原生调用Js方法并支持异步回调
+ Js调用名称空间可自由配置，统一管理命名空间
+ 支持Js调用原生方法多次回调，如果不想多次回调可以删除回调方法
+ 支持部分Js框架中window并非顶级window

# API介绍

- `callJsFunction(function: String)`

    原生调用Js的方法，不支持传递参数和回调
    
    > 参数
    
    + function:调用Js的方法名称
    
- `callJsFunction(function: String, callback: JsCallback?)`

    原生调用Js的方法，不支持传递参数但支持回调
    
    > 参数
    
    + function:调用Js的方法名称
    + callback:回调函数
    
- `callJsFunction(function: String, data: String?)`

    原生调用Js的方法，支持传递参数，但不支持回调
    
    > 参数
    
    + function:调用Js的方法名称
    + data:字符串，传递给Js的参数

- `callJsFunction(function: String, data: String?, callback: JsCallback?)`

    原生调用Js的方法，支持传递参数和回调
    
    > 参数
    
    + function:调用Js的方法名称
    + data:字符串，传递给Js的参数
    + callback:回调函数
    
- `jsCallName()`

    Js调用原生的API命名空间，SDK统一了命名空间，默认为`JsBridge`，整个Js调用中只会使用这个命名，当然，如果是你不喜欢这个命名，或者需要根据项目更改为项目有关的命名，可以自定义WebView继承至[BaseWebView](https://github.com/ChinaLike/JsBridge/blob/main/library/src/main/java/com/core/web/base/BaseWebView.kt)重写该方法。
    
- `getWindow()`

    Js中获取window的方法，建议不要随意修改。在特殊的Js框架中，获取系统的window需要window.window才能获取到，所以这时候可以自定义WebView继承至[BaseWebView](https://github.com/ChinaLike/JsBridge/blob/main/library/src/main/java/com/core/web/base/BaseWebView.kt)重写该方法。

# 如何使用

## 依赖引入

> Step 1.先在 build.gradle(Project:XXX) 的 repositories 添加:

	allprojects {
		repositories {
			...
			maven { url "https://jitpack.io" }
		}
	}
> Step 2. 然后在 build.gradle(Module:XXX) 的 dependencies 添加:

	dependencies {
           implementation 'com.github.ChinaLike:JsBridge:0.0.1'
	}
        
## 新建Js调用原生方法的类，参考：[JsBridgeToast](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/JsBridgeToast.kt)

```js
package com.like.jsbridge

import android.content.Context
import android.os.Handler
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import com.core.web.Callback
import com.core.web.CallbackBean

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

    @JavascriptInterface
    fun nativeSyncCallback():String{
        return "原生同步回调"
    }
}
```

## 新建MainActivity和xml文件,参考：[MainActivityKotlin](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/MainActivityKotlin.java)和[activity_main](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/res/layout/activity_main.xml)

+ 在xml文件中引入JsBridgeWebView

```js
<com.core.web.JsBridgeWebView
        android:id="@+id/webView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
```
> 当然，如果没有特殊需要使用`JsBridgeWebView`就可以了，如果有定制WebView，则引入自己定制那个WebView就可以了，但是自定义的WebView需要继承[BaseWebView](https://github.com/ChinaLike/JsBridge/blob/main/library/src/main/java/com/core/web/base/BaseWebView.kt)

+ 在Activity中调用`addJavascriptInterface`添加

```js
import com.core.web.JsBridgeWebView;

class MainActivityKotlin : AppCompatActivity() {

    private var webView: JsBridgeWebView? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webView = findViewById(R.id.webView)
        webView?.addJavascriptInterface(JsBridgeToast(this))
        webView?.loadUrl("file:///android_asset/test.html")
}
```

> `注意：`webView的`addJavascriptInterface`方法可以多次调用，开发时可以根据业务功能进行解耦，`addJavascriptInterface`方法支持传递一个或两个参数，传递两个参数时第二个参数无效。

## 在Js中调用原生方法，参考：[test.html](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/assets/test.html)

+ Js调用原生无参无回调方法

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeNoArgAndNoCallback(){
        Toast.makeText(context,"调用原生无参数无回调方法",Toast.LENGTH_SHORT).show()
    }
    ```
    
    - Java代码
    
    ```js
    @JavascriptInterface
    public void nativeNoArgAndNoCallback(){
        Toast.makeText(context,"调用原生无参数无回调方法",Toast.LENGTH_SHORT).show();
    }
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeNoArgAndNoCallback();
    ```

+ Js调用原生无参有回调方法

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeNoArgAndCallback(callback: Callback){
        callback.success()
    }
    ```
    
    - Java代码
    
    ```js
    @JavascriptInterface
    public void nativeNoArgAndCallback(Callback callback){
        callback.success();
    }
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeNoArgAndCallback(function(result){
        alert(JSON.stringify(result));
    });
    ```
  
+ Js调用原生有参无回调方法

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeArgAndNoCallback(params:String){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
    }
    ```
    
    - Java代码
    
    ```js
    @JavascriptInterface
    public void nativeArgAndNoCallback(String params){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
    }
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeArgAndNoCallback("调用原生有参数无回调方法");
    ```

+ Js调用原生有参有回调方法

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeArgAndCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success()
    }
    ```
    
    - Java代码
    
    ```js
    @JavascriptInterface
    public void nativeArgAndCallback(String params,Callback callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show();
        callback.success();
    }
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeArgAndCallback("调用原生有参数有回调方法",function(result){
        alert(JSON.stringify(result))
    });
    ```
    
+ Js调用原生有参有回调方法，且只能回调一次

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(true)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }
    ```
    
    - Java代码
    
    ```js
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
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeDeleteCallback("调用原生方法后删除回调",function(result){
				alert(JSON.stringify(result))
			},function(error){
				alert(JSON.stringify(result))
			});
    ```

+ Js调用原生有参有回调方法，且能回调多次

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeNoDeleteCallback(params:String,callback: Callback){
        Toast.makeText(context,params,Toast.LENGTH_SHORT).show()
        callback.success(false)
        Handler().postDelayed(Runnable {
            callback.error(1,"错误回调")
        },3000)
    }
    ```
    
    - Java代码
    
    ```js
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
    ```
    
    - Js代码
    
    ```js
    JsBridge.nativeNoDeleteCallback("调用原生方法后不删除回调",function(result){
				alert(JSON.stringify(result))
			},function(error){
				alert(JSON.stringify(error))
			});
    ```
    
+ Js调用原生同步回调数据

    - Kotlin代码
    
    ```js
    @JavascriptInterface
    fun nativeSyncCallback():String{
        return "原生同步回调"
    }
    ```
    
    - Java代码
    
    ```js
    @JavascriptInterface
    public String nativeSyncCallback(){
        return "原生同步回调";
    }
    ```
    
    - Js代码
    
    ```js
    var  result = JsBridge.nativeSyncCallback();
    alert(result)
    ```
    
> `注意：`被`@JavascriptInterface`注解的方法，只支持最多两个参数，不论顺序。`无参数：`代表Js不传递参数过来也不回调；`一个参数：`可以是传递过来的参数或回调；`两个参数：`一个为参数，一个是回调。

## 在原生中调用Js方法，参考：[MainActivity](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/MainActivity.java)

+ 原生调用JS无参数无回调

    - Js代码
    
    ```js
    function jsNoArgAndNoCallback(){
        alert("原生调用JS无参数无回调");
    }
    ```
    
    - Java代码
    
    ```js
    webView.callJsFunction("jsNoArgAndNoCallback");
    ```
    
    - Kotlin代码
    
    ```js
    webView?.callJsFunction("jsNoArgAndNoCallback")
    ```

+ 原生调用Js无参数有回调

    - Js代码
    
    ```js
    function jsNoArgAndCallback(callback){
            alert("原生调用JS无参数有回调");
            callback("我是JS回调数据");
    }
    ```
    
    - Java代码
    
    ```js
    webView.callJsFunction("jsNoArgAndCallback", callback -> Toast.makeText(this, "" + callback, Toast.LENGTH_SHORT).show());
    ```
    
    - Kotlin代码
    
    ```js
    webView?.callJsFunction("jsNoArgAndCallback", object : JsCallback {
            override fun onCallback(callback: Any) {
                Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
            }
        })
    ```

+ 原生调用Js有参数无回调

    - Js代码
    
    ```js
    function jsArgAndNoCallback(params){
            alert(params);
    }
    ```
    
    - Java代码
    
    ```js
    webView.callJsFunction("jsArgAndNoCallback", "原生传递过来的参数");
    ```
    
    - Kotlin代码
    
    ```js
    webView?.callJsFunction("jsArgAndNoCallback","原生传递过来的参数")
    ```

+ 原生调用Js有参数有回调（可回调多次）

    - Js代码
    
    ```js
    function jsArgAndCallback(params,callback){
            alert(params);
            callback("我是JS第一次回调数据");
            setTimeout(function() {
                    callback("我是JS第二次回调数据");
            }, 500);
    }
    ```
    
    - Java代码
    
    ```js
    webView.callJsFunction("jsArgAndCallback", "原生传递过来的参数", callback -> Toast.makeText(this, "" + callback, Toast.LENGTH_SHORT).show());
    ```
    
    - Kotlin代码
    
    ```js
    webView?.callJsFunction("jsArgAndCallback","原生传递过来的参数", object : JsCallback {
            override fun onCallback(callback: Any) {
                Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
            }
        })
    ```
    
+ 调用Js有参数有回调（只能回调一次）

    - Js代码
    
    ```js
    function jsArgAndDeleteCallback(params,callback){
            alert(params);
            callback("我是JS第一次回调数据",true);
            setTimeout(function() {
                    callback("我是JS第二次回调数据");
            }, 500);
    }
    ```
    
    - Java代码
    
    ```js
    webView.callJsFunction("jsArgAndDeleteCallback", "原生传递过来的参数", callback -> Toast.makeText(this, "" + callback, Toast.LENGTH_SHORT).show());
    ```
    
    - Kotlin代码
    
    ```js
    webView?.callJsFunction("jsArgAndDeleteCallback","原生传递过来的参数", object : JsCallback {
            override fun onCallback(callback: Any) {
                Toast.makeText(this@MainActivityKotlin, "$callback", Toast.LENGTH_SHORT).show()
            }
        })
    ```
    
> `注意：`原生调用Js代码，Js的方法最多支持两个参数，`无参数：`代表原生不传递参数过来也不用回调给原生； `一个参数：`可以是参数也可以是回调；`两个参数：`有序，第一个为参数，第二个为回调，代表原生传递过来参数，且需要回调给原生