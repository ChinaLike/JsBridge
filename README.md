# JsBridge
[![](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html)  [![API](https://img.shields.io/badge/API-17%2B-blue.svg?style=flat)](https://android-arsenal.com/api?level=17)  [![](https://jitpack.io/v/ChinaLike/JsBridge.svg)](https://jitpack.io/#ChinaLike/JsBridge)  [![Gradle-4.1.2](https://img.shields.io/badge/Gradle-4.1.2-brightgreen.svg)](https://img.shields.io/badge/Gradle-4.1.2-brightgreen.svg)  [![](https://img.shields.io/badge/language-kotlin-brightgreen.svg)](https://kotlinlang.org/)

## SDK支持

   + Js调用原生方法并支持异步回调和同步回调
   + 原生调用Js方法并支持异步回调
   + Js调用名称空间可自由配置
   + 支持Js调用原生方法多次回调，如果不想多次回调可以删除回调方法
   + 支持部分Js框架中window并非顶级window

## 如何使用

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
	
## 示例

   ![演示视频](https://github.com/ChinaLike/JsBridge/blob/main/screenshots/1623390971414301.gif)

   [请下载本工程，运行工程即可查看示例](https://github.com/ChinaLike/JsBridge)
	
## 使用

1. 新建一个类，实现Js需要调用的方法,参考：[JsBridgeToast](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/JsBridgeToast.kt)
    
    ```
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
    由于安全原因，所有需要被Js调用的方法必有`@JavascriptInterface`注解
    
2. 添加JsBridgeToast到JsBridgeWebView,参考：[MainActivity](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/MainActivity.java)
    
    ```
    webView = findViewById(R.id.webView);
    webView.addJavascriptInterface(new JsBridgeToast(this));
    ```
    
3. 在Javascript中调用原生API,参考：[test.html](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/assets/test.html)
    
    ```javascript
    JsBridge.nativeNoArgAndNoCallback();
    ```
    
4. 在Java中调用 Javascript API,参考：[MainActivity](https://github.com/ChinaLike/JsBridge/blob/main/app/src/main/java/com/like/jsbridge/MainActivity.java)
    
    ```
    webView.callJsFunction("jsArgAndDeleteCallback", "原生传递过来的参数", callback -> Toast.makeText(MainActivity.this, "" + callback, Toast.LENGTH_SHORT).show())
    ```
