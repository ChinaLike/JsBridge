package com.core.web

import com.core.bridge.enums.BridgeType
import com.core.bridge.interfaces.IBridgeMethod
import com.core.bridge.model.BridgeMeta
import com.core.util.ClassUtils
import com.core.web.base.BaseWebView

/**
 * js注入管理类
 * @author like
 * @date 5/24/21 11:32 AM
 */
class JsInject(private val webView: BaseWebView) {

    private var methodList: MutableList<BridgeMeta> = mutableListOf()

    private val bridgeName: String by lazy { webView.jsCallName() }

    /**
     * 闭包返回名称
     */
    private val closureResultName = bridgeName + "_app_closure_result_N3hsaI8sls0_usj3"


    init {
        val clazzName = ClassUtils.getMethodByGroup(webView.getGroup())
        if (!clazzName.isNullOrEmpty()) {
            val clazz = Class.forName(clazzName)
            val constructor = clazz.getConstructor()
            val instance = constructor.newInstance()
            if (instance is IBridgeMethod) {
                methodList = instance.loadMethod().toMutableList()
            }
        }
    }

    /**
     * 注入js
     */
    fun injectJs(): String {
        var jsStringBuffer = StringBuffer()
        //注入头部
        jsStringBuffer.append("\n\t\tvar $closureResultName = (function() {\n")
        //注入参数
        jsStringBuffer.append("\t\t\tvar id = 0;\n")
        jsStringBuffer.append("\t\t\tvar jsCallNativeSuccessCallback = {};\n")
        jsStringBuffer.append("\t\t\tvar jsCallNativeFailCallback = {};\n\n")
        //处理数据
        jsStringBuffer.append(
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
                    "\t\t\t}\n"
        )
        //生成回调ID
        jsStringBuffer.append(
            "\t\t\tfunction getCallbackId(methodName) {\n" +
                    "\t\t\t\tvar timestamp = new Date().getTime();\n" +
                    "\t\t\t\tid++;\n" +
                    "\t\t\t\treturn String(methodName + timestamp + id);\n" +
                    "\t\t\t}\n"
        )
        //处理回调及参数
        jsStringBuffer.append(
            "\t\t\tfunction handleCallbackDataAndParams(params, cbId, failCbid) {\n" +
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
                    "\t\t\t\t\t\tif (failCbid && failCbid != null && typeof failCbid != 'undefined') {\n" +
                    "\t\t\t\t\t\t\tjsCallNativeFailCallback[failCbid] = params[1];\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t} else if (params.length == 3 && typeof params[0] != 'function' && typeof params[1] === 'function' && typeof params[2] === 'function') {\n" +
                    "\t\t\t\t\t\tparamsData = dataToString(params[0]);\n" +
                    "\t\t\t\t\t\tjsCallNativeSuccessCallback[cbId] = params[1];\n" +
                    "\t\t\t\t\t\tif (failCbid && failCbid != null && typeof failCbid != 'undefined') {\n" +
                    "\t\t\t\t\t\t\tjsCallNativeFailCallback[failCbid] = params[2];\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t\treturn paramsData;\n" +
                    "\t\t\t}\n"
        )
        //兼容Window
        jsStringBuffer.append(
            "\t\t\tfunction getWindow() {\n" +
                    "\t\t\t\tif (window.${bridgeName}) {\n" +
                    "\t\t\t\t\treturn window.${bridgeName};\n" +
                    "\t\t\t\t} else {\n" +
                    "\t\t\t\t\treturn ${webView.getWindow()}.${bridgeName};\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n"
        )
        //匹配异步或异步+同步方法，同步方法可不用
        methodList.forEach {
            if (it.type == BridgeType.SYNCHRONIZE) return@forEach
            jsStringBuffer.append(
                "\t\t\t${bridgeName}.${it.methodName} = function() {\n" +
                        "\t\t\t\ttry {\n" +
                        "\t\t\t\t\tvar fn = eval('getWindow().${it.alias}');\n" +
                        "\t\t\t\t\tif (typeof fn === 'function') {\n" +
                        "\t\t\t\t\t\tvar hasFailCallback = ${it.parametersClassNameList.size == 3};\n" +
                        "\t\t\t\t\t\tvar cbId = getCallbackId(\"${it.methodName}\");\n" +
                        "\t\t\t\t\t\tvar failCbId = hasFailCallback ? getCallbackId(\"${it.methodName}\") : null;\n" +
                        "\t\t\t\t\t\tvar paramsData = handleCallbackDataAndParams(arguments, cbId, failCbId);\n" +
                        "\t\t\t\t\t\tvar paramsNumber = ${it.parametersClassNameList.size};\n" +
                        "\t\t\t\t\t\tif(paramsNumber == 0){\n" +
                        "\t\t\t\t\t\t\treturn fn.call(getWindow());\n" +
                        "\t\t\t\t\t\t}else if(paramsNumber == 1){\n" +
                        "\t\t\t\t\t\t\treturn fn.call(getWindow(), ${if (it.verifyParams()) "paramsData" else "cbId"});\n" +
                        "\t\t\t\t\t\t}else if(paramsNumber == 2){\n" +
                        "\t\t\t\t\t\t\treturn fn.call(getWindow(), ${if (it.verifyParams()) "paramsData" else "cbId"}, ${if (it.verifyParams()) "cbId" else "failCbId"});\n" +
                        "\t\t\t\t\t\t}else{\n" +
                        "\t\t\t\t\t\t\treturn fn.call(getWindow(), paramsData, cbId, failCbId);\n" +
                        "\t\t\t\t\t\t}\n" +
                        "\t\t\t\t\t} else {\n" +
                        "\t\t\t\t\t\tconsole.log(\"fn 不是一个函数\");\n" +
                        "\t\t\t\t\t}\n" +
                        "\t\t\t\t} catch (e) {\n" +
                        "\t\t\t\t\tconsole.log(e);\n" +
                        "\t\t\t\t}\n" +
                        "\t\t\t}\n"
            )
        }
        //回调
        jsStringBuffer.append(
            "\t\t\t${bridgeName}.on = function(cbId, result, deleteId) {\n" +
                    "\t\t\t\tconsole.log(\"cbId:\" + cbId);\n" +
                    "\t\t\t\tconsole.log(\"返回结果：\" + JSON.stringify(result));\n" +
                    "\t\t\t\tvar successCallback = jsCallNativeSuccessCallback[cbId];\n" +
                    "\t\t\t\tvar failCallback = jsCallNativeFailCallback[cbId];\n" +
                    "\t\t\t\ttry {\n" +
                    "\t\t\t\t\tvar resultData = result;\n" +
                    "\t\t\t\t\tif (typeof result == 'string') {\n" +
                    "\t\t\t\t\t\tresultData = JSON.parse(result);\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t\tvar successCallbackExist = successCallback && successCallback != null && (typeof successCallback != 'undefined');\n" +
                    "\t\t\t\t\tvar failCallback = failCallback && failCallback != null && (typeof failCallback != 'undefined');\n" +
                    "\t\t\t\t\tif (successCallbackExist && !failCallback) {\n" +
                    "\t\t\t\t\t\tsuccessCallback(resultData);\n" +
                    "\t\t\t\t\t\tif (deleteId) {\n" +
                    "\t\t\t\t\t\t\tdelete jsCallNativeSuccessCallback[cbId];\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t} else if (!successCallbackExist && failCallback) {\n" +
                    "\t\t\t\t\t\tfailCallback(resultData);\n" +
                    "\t\t\t\t\t\tif (deleteId) {\n" +
                    "\t\t\t\t\t\t\tdelete jsCallNativeFailCallback[cbId];\n" +
                    "\t\t\t\t\t\t}\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t} catch (e) {\n" +
                    "\t\t\t\t\tthrow new Error(\"回调数据异常\");\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n"
        )
        //注入公共方法
        jsStringBuffer.append("\t\t\treturn {\n" +
                "\t\t\t\tdataToString: function(params) {\n" +
                "\t\t\t\t\treturn dataToString(params);\n" +
                "\t\t\t\t},\n" +
                "\t\t\t\tgetWindow: function() {\n" +
                "\t\t\t\t\treturn getWindow();\n" +
                "\t\t\t\t},\n" +
                "\t\t\t}\n")
        //注入尾部
        jsStringBuffer.append("\t\t})();\n")
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
                "\t\t\t\t\t${closureResultName}.getWindow().jsCallbackDispatch('${callbackId}', ${closureResultName}.dataToString(data),isDelete);\n" +
                "\t\t\t\t}\n\n"
    }

}