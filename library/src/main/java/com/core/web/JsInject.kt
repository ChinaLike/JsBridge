package com.core.web

import com.core.util.ReflectUtil

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
        val jsBridgeName = webView.jsCallName()
        val innerJavascriptInterfaceName = webView.innerJavascriptInterfaceName
        var jsStringBuffer = StringBuffer()

        //提供给Native的对象处理
        jsStringBuffer.append(
            "\n\t\tfunction ${jsBridgeName}_handle_data(params) {\n" +
                    "\t\t\tvar resultParams;\n" +
                    "\t\t\tif (typeof params === 'undefined' || typeof params === 'function') {\n" +
                    "\t\t\t\tresultParams = \"\";\n" +
                    "\t\t\t} else if (typeof params === 'object') {\n" +
                    "\t\t\t\ttry {\n" +
                    "\t\t\t\t\tresultParams = JSON.stringify(params);\n" +
                    "\t\t\t\t} catch (e) {\n" +
                    "\t\t\t\t\tconsole.log(e);\n" +
                    "\t\t\t\t\tresultParams = \"\";\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t} else {\n" +
                    "\t\t\t\tresultParams = params;\n" +
                    "\t\t\t}\n" +
                    "\t\t\treturn resultParams;\n" +
                    "\t\t}\n\n"
        )

        //回调ID处理
        jsStringBuffer.append(
            "\t\tfunction ${jsBridgeName}_CBID(methodName) {\n" +
                    "\t\t\tvar timestamp = new Date().getTime();\n" +
                    "\t\t\t${jsBridgeName}.id++;\n" +
                    "\t\t\treturn String(methodName + timestamp + ${jsBridgeName}.id);\n" +
                    "\t\t}\n\n"
        )

        //处理调用对象及回调方法
        jsStringBuffer.append(
            "\t\tfunction ${jsBridgeName}_Object_Save(params, cbId) {\n" +
                    "\t\t\tvar paramsData = \"\";\n" +
                    "\t\t\tif (params && params.length) {\n" +
                    "\t\t\t\tif (params.length == 1 && typeof params[0] != 'function') {\n" +
                    "\t\t\t\t\tparamsData = ${jsBridgeName}_handle_data(params[0]);\n" +
                    "\t\t\t\t} else if (params.length == 1 && typeof params[0] === 'function') {\n" +
                    "\t\t\t\t\t${jsBridgeName}.successCallback[cbId] = params[0];\n" +
                    "\t\t\t\t} else if (params.length == 2 && typeof params[0] != 'function' && typeof params[1] === 'function') {\n" +
                    "\t\t\t\t\tparamsData = ${jsBridgeName}_handle_data(params[0]);\n" +
                    "\t\t\t\t\t${jsBridgeName}.successCallback[cbId] = params[1];\n" +
                    "\t\t\t\t} else if (params.length == 2 && typeof params[0] === 'function' && typeof params[1] === 'function') {\n" +
                    "\t\t\t\t\t${jsBridgeName}.successCallback[cbId] = params[0];\n" +
                    "\t\t\t\t\t${jsBridgeName}.failCallback[cbId] = params[1];\n" +
                    "\t\t\t\t} else if (params.length == 3 && typeof params[0] != 'function' && typeof params[1] === 'function' && typeof params[\n" +
                    "\t\t\t\t\t\t2] === 'function') {\n" +
                    "\t\t\t\t\tparamsData = ${jsBridgeName}_handle_data(params[0]);\n" +
                    "\t\t\t\t\t${jsBridgeName}.successCallback[cbId] = params[1];\n" +
                    "\t\t\t\t\t${jsBridgeName}.failCallback[cbId] = params[2];\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t\treturn paramsData;\n" +
                    "\t\t}\n\n"
        )

        //获取Window
        jsStringBuffer.append(
            "\t\tfunction ${jsBridgeName}_window(){\n" +
                    "\t\t\tif (window.${innerJavascriptInterfaceName}) {\n" +
                    "\t\t\t\treturn window.${innerJavascriptInterfaceName};\n" +
                    "\t\t\t}else{\n" +
                    "\t\t\t\treturn ${webView.getWindow()}.${innerJavascriptInterfaceName};\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n\n"
        )

        //方法注入
        jsStringBuffer.append(
            "\t\twindow.${jsBridgeName} = {\n" +
                    "\t\t\tid: 1,\n" +
                    "\t\t\tsuccessCallback: {},\n" +
                    "\t\t\tfailCallback: {},\n"
        )

        //注入方法
        webView.javascriptInterfaceList.forEach { bean ->
            //取出需要注入的方法
            val methods = ReflectUtil.getMethods(bean.`object`, "JavascriptInterface")
            methods?.forEach {
                val value = it.parameterTypes
                var requireParams = value != null && value.isNotEmpty()
                jsStringBuffer.append(
                    "\t\t\t${it.name}: function() {\n" +
                            "\t\t\t\tvar cbId = ${jsBridgeName}_CBID(\"${it.name}\");\n" +
                            "\t\t\t\tvar paramsData = ${jsBridgeName}_Object_Save(arguments, cbId);\n" +
                            "\t\t\t\t${jsBridgeName}_window().nativeDispatch(\"${bean.`object`.javaClass.name}\", \"${it.name}\", paramsData, cbId);\n" +
                            "\t\t\t},\n"
                )
            }
        }

        jsStringBuffer.append(
            "\t\t\ton: function(cbId, result, deleteId) {\n" +
                    "\t\t\t\tvar success = this.successCallback[cbId];\n" +
                    "\t\t\t\tvar fail = this.failCallback[cbId];\n" +
                    "\t\t\t\ttry {\n" +
                    "\t\t\t\t\tvar resultData = result;\n" +
                    "\t\t\t\t\tif (typeof result == 'string') {\n" +
                    "\t\t\t\t\t\tresultData = JSON.parse(result);\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t\tif (resultData.code == 0 && success) {\n" +
                    "\t\t\t\t\t\tsuccess(resultData);\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t\tif (resultData.code != 0 && fail) {\n" +
                    "\t\t\t\t\t\tfail(resultData);\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t\tif (deleteId) {\n" +
                    "\t\t\t\t\t\tdelete this.successCallback[cbId];\n" +
                    "\t\t\t\t\t\tdelete this.failCallback[cbId];\n" +
                    "\t\t\t\t\t}\n" +
                    "\t\t\t\t} catch (e) {\n" +
                    "\t\t\t\t\tthrow new Error(\"回调数据异常\");\n" +
                    "\t\t\t\t}\n" +
                    "\t\t\t}\n" +
                    "\t\t}\n\n"
        )

        return jsStringBuffer.toString()
    }

}