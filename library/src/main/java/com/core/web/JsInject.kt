package com.core.web

import com.core.util.ReflectUtil
import com.core.web.base.BaseWebView

/**
 * js注入管理类
 * @author like
 * @date 5/24/21 11:32 AM
 */
class JsInject(private val webView: BaseWebView) {

    /**
     * 注入js
     */
    fun injectJs(): String {
        val innerJavascriptInterfaceName = webView.innerJavascriptInterfaceName
        var jsStringBuffer = StringBuffer()

        //注入一个检测方法
        jsStringBuffer.append(
            "\n\t\tfunction ${webView.injectVerifyMethod()}(){\n" +
                    "\t\t\treturn window.${webView.jsCallName()} !=null\n" +
                    "\t\t}\n"
        )
        //注入内容
        jsStringBuffer.append(
            "\n\t\t(function(){\n" +
                    "\t\t\tif(window.${webView.jsCallName()}){\n" +
                    "\t\t\t\treturn;\n" +
                    "\t\t\t}\n" +
                    "\n" +
                    "\t\t\tvar id = 0;\n" +
                    "\t\t\t\n" +
                    "\t\t\tvar jsCallNativeSuccessCallback = {};\n" +
                    "\t\t\tvar jsCallNativeFailCallback = {};\n\n"
        )

            //处理数据为字符串格式
            .append(
                "\t\t\tfunction dataToString(params) {\n" +
                        "\t\t\t\tvar resultParams;\n" +
                        "\t\t\t\tif (typeof params === 'undefined' || typeof params === 'function') {\n" +
                        "\t\t\t\t\tresultParams = \"\";\n" +
                        "\t\t\t\t} else if (typeof params === 'object') {\n" +
                        "\t\t\t\t\ttry {\n" +
                        "\t\t\t\t\t\tresultParams = JSON.stringify(params);\n" +
                        "\t\t\t\t\t} catch (e) {\n" +
                        "\t\t\t\t\t\tconsole.log(e);\n" +
                        "\t\t\t\t\t\tresultParams = \"\";\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t} else {\n" +
                        "\t\t\t\t\tresultParams = params;\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\treturn resultParams;\n" +
                        "\t\t\t}\n\n"
            )
            //生成回调ID
            .append(
                "\t\t\tfunction getCallbackId(methodName) {\n" +
                        "\t\t\t\tvar timestamp = new Date().getTime();\n" +
                        "\t\t\t\tid++;\n" +
                        "\t\t\t\treturn String(methodName + timestamp + id);\n" +
                        "\t\t\t}\n\n"
            )

            //处理参数与回调数据
            .append(
                "\t\t\tfunction handleCallbackDataAndParams(params, cbId) {\n" +
                        "\t\t\t\tvar paramsData = \"\";\n" +
                        "\t\t\t\tif (params && params.length) {\n" +
                        "\t\t\t\t\tif (params.length == 1 && typeof params[0] != 'function') {\n" +
                        "\t\t\t\t\t\tparamsData = dataToString(params[0]);\n" +
                        "\t\t\t\t\t} else if (params.length == 1 && typeof params[0] === 'function') {\n" +
                        "\t\t\t\t\t\tjsCallNativeSuccessCallback[cbId] = params[0];\n" +
                        "\t\t\t\t\t} else if (params.length == 2 && typeof params[0] != 'function' && typeof params[1] === 'function') {\n" +
                        "\t\t\t\t\t\tparamsData = dataToString(params[0]);\n" +
                        "\t\t\t\t\t\tjsCallNativeSuccessCallback[cbId] = params[1];\n" +
                        "\t\t\t\t\t} else if (params.length == 2 && typeof params[0] === 'function' && typeof params[1] === 'function') {\n" +
                        "\t\t\t\t\t\tjsCallNativeSuccessCallback[cbId] = params[0];\n" +
                        "\t\t\t\t\t\tjsCallNativeFailCallback[cbId] = params[1];\n" +
                        "\t\t\t\t\t} else if (params.length == 3 && typeof params[0] != 'function' && typeof params[1] === 'function' && typeof params[\n" +
                        "\t\t\t\t\t\t\t2] === 'function') {\n" +
                        "\t\t\t\t\t\tparamsData = dataToString(params[0]);\n" +
                        "\t\t\t\t\t\tjsCallNativeSuccessCallback[cbId] = params[1];\n" +
                        "\t\t\t\t\t\tjsCallNativeFailCallback[cbId] = params[2];\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t\treturn paramsData;\n" +
                        "\t\t\t}\n\n"
            )


            .append(
                "\t\tfunction getWindow(){\n" +
                        "\t\t\tif (window.${innerJavascriptInterfaceName}) {\n" +
                        "\t\t\t\treturn window.${innerJavascriptInterfaceName};\n" +
                        "\t\t\t}else{\n" +
                        "\t\t\t\treturn ${webView.getWindow()}.${innerJavascriptInterfaceName};\n" +
                        "\t\t\t}\n" +
                        "\t\t}\n\n"
            )

            .append("\t\t\twindow.${webView.jsCallName()} = {\n")

        //注入方法
        webView.javascriptInterfaceList.forEach { bean ->
            //取出需要注入的方法
            val methods = ReflectUtil.getMethods(bean.`object`, "JavascriptInterface")
            methods?.forEach {
                jsStringBuffer.append(
                    "\t\t\t${it.name}: function() {\n" +
                            "\t\t\t\t\tvar cbId = getCallbackId(\"${it.name}\");\n" +
                            "\t\t\t\t\tvar paramsData = handleCallbackDataAndParams(arguments, cbId);\n" +
                            "\t\t\t\treturn getWindow().nativeDispatch(\"${bean.`object`.javaClass.name}\", \"${it.name}\", paramsData, cbId);\n" +
                            "\t\t\t},\n"
                )
            }
        }

        //注入回调
        jsStringBuffer.append(
            "\t\t\t\ton: function(cbId, result, deleteId) {\n" +
                    "\t\t\t\t\tconsole.log(\"cbId:\" + cbId+\",返回结果：\" + JSON.stringify(result));\n" +
                    "\t\t\t\t\tvar success = jsCallNativeSuccessCallback[cbId];\n" +
                    "\t\t\t\t\tvar fail = jsCallNativeFailCallback[cbId];\n" +
                    "\t\t\t\t\ttry {\n" +
                    "\t\t\t\t\t\tvar resultData = result;\n" +
                    "\t\t\t\t\t\tif (typeof result == 'string') {\n" +
                    "\t\t\t\t\t\t\tresultData = JSON.parse(result);\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t\tif (resultData.code == 0 && success) {\n" +
                    "\t\t\t\t\t\t\tsuccess(resultData);\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t\tif (resultData.code != 0 && fail) {\n" +
                    "\t\t\t\t\t\t\tfail(resultData);\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t\tif (deleteId) {\n" +
                    "\t\t\t\t\t\t\tdelete jsCallNativeSuccessCallback[cbId];\n" +
                    "\t\t\t\t\t\t\tdelete jsCallNativeFailCallback[cbId];\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t} catch (e) {\n" +
                    "\t\t\t\t\t\tthrow new Error(\"回调数据异常\");\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t},\n"
        )

            .append(
                "\t\t\t\tdataToString:dataToString,\n" +
                        "\t\t\t\tgetWindow:getWindow\n" +
                        "\t\t\t}\n" +
                        "\n" +
                        "\t\t})();\n\n"
            )


        return jsStringBuffer.toString()
    }

    /**
     * 回调Js注入
     */
    fun callbackJsInject(callbackId: String): String {
        return "function(data,deleteCbId){\n" +
                "\t\t\t\t\tvar isDelete = false;\n" +
                "\t\t\t\t\tif (typeof deleteCbId === 'boolean') {\n" +
                "\t\t\t\t\t\tisDelete = deleteCbId;\n" +
                "\t\t\t\t\t}\n" +
                "\t\t\t\t\twindow.${webView.jsCallName()}.getWindow().jsCallbackDispatch('${callbackId}', window.${webView.jsCallName()}.dataToString(data),isDelete);\n" +
                "\t\t\t\t}\n\n"
    }

}