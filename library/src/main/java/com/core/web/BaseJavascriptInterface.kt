package com.core.web

import android.text.TextUtils
import android.webkit.JavascriptInterface
import com.core.util.ReflectUtil

/**
 * 基础方法注入类
 * @author like
 * @date 5/26/21 3:49 PM
 */
class BaseJavascriptInterface(private val webView: BaseWebView) {

    /**
     * js调用原生的方法，所有方法从这里分发
     * @param [classPath] 类路径
     * @param [method] 方法名
     * @param [data] 数据
     * @param [cbId] 回调Id
     */
    @JavascriptInterface
    fun nativeDispatch(classPath: String, method: String, data: String, cbId: String):String {
        var resultData:Any? = null
        webView.javascriptInterfaceList?.forEach { bean ->
            //对应路径下的类
            if (bean.`object`.javaClass.name == classPath) {
                val methods = ReflectUtil.getMethods(bean.`object`, "JavascriptInterface")
                methods?.forEach {
                    //找到对应的方法
                    if (it.name == method) {
                        val params = it.parameterTypes
                        when (params?.size) {
                            0 -> {
                                //没有参数
                                resultData = it.invoke(bean.`object`)
                            }
                            1 -> {
                                //一个参数
                                when (params[0].simpleName) {
                                    Callback::class.java.simpleName -> {
                                        resultData = it.invoke(bean.`object`, Callback(webView, cbId))
                                    }
                                    Int::class.java.simpleName -> {
                                        resultData = it.invoke(bean.`object`, data.toInt())
                                    }
                                    Double::class.java.simpleName -> {
                                        resultData = it.invoke(bean.`object`, data.toDouble())
                                    }
                                    Float::class.java.simpleName -> {
                                        resultData = it.invoke(bean.`object`, data.toFloat())
                                    }
                                    Boolean::class.java.simpleName -> {
                                        resultData = it.invoke(bean.`object`, data.toBoolean())
                                    }
                                    else -> {
                                        resultData = it.invoke(bean.`object`, data)
                                    }
                                }
                            }
                            2 -> {
                                //两个参数
                                val params1 = params[0].simpleName
                                val params2 = params[1].simpleName
                                if (params1 == Callback::class.java.simpleName && params2 != Callback::class.java.simpleName) {
                                    when (params2) {
                                        Int::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    Callback(webView, cbId),
                                                    data.toInt()
                                            )
                                        }
                                        Double::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    Callback(webView, cbId),
                                                    data.toDouble()
                                            )
                                        }
                                        Float::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    Callback(webView, cbId),
                                                    data.toFloat()
                                            )
                                        }
                                        Boolean::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    Callback(webView, cbId),
                                                    data.toBoolean()
                                            )
                                        }
                                        else -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    Callback(webView, cbId),
                                                    data
                                            )
                                        }
                                    }
                                } else if (params1 != Callback::class.java.simpleName && params2 == Callback::class.java.simpleName) {
                                    when (params1) {
                                        Int::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    data.toInt(),
                                                    Callback(webView, cbId)
                                            )
                                        }
                                        Double::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    data.toDouble(),
                                                    Callback(webView, cbId)
                                            )
                                        }
                                        Float::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    data.toFloat(),
                                                    Callback(webView, cbId)
                                            )
                                        }
                                        Boolean::class.java.simpleName -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    data.toBoolean(),
                                                    Callback(webView, cbId)
                                            )
                                        }
                                        else -> {
                                            resultData = it.invoke(
                                                    bean.`object`,
                                                    data,
                                                    Callback(webView, cbId)
                                            )
                                        }
                                    }
                                } else {
                                    throw IllegalArgumentException("参数错误，请检查${it.name}方法参数!")
                                }
                            }
                            else -> {
                                throw IllegalArgumentException("参数错误，请检查${it.name}方法参数!")
                            }
                        }
                    }
                }
            }
        }

        return if (resultData == null) "" else resultData.toString()
    }

    /**
     * Js回调数据给原生
     */
    @JavascriptInterface
    fun jsCallbackDispatch(cbId: String, data: String, deleteCbId: Boolean) {
        if (webView == null || TextUtils.isEmpty(cbId) || webView.callbackManager == null) {
            return
        }
        val callbackManager: JsCallback? = webView.callbackManager[cbId]
        callbackManager?.onCallback(data)
        //移除回调
        if (callbackManager != null && deleteCbId) {
            webView.callbackManager.remove(cbId)
        }
    }

}