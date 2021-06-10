package com.core.web

/**
 * WebView 需实现方法
 * @author like
 * @date 6/1/21 6:06 PM
 */
interface IWebView {

    /**
     * 回调数据给Js
     * @param [cbId] 回调的Id
     * @param [data] 回调的数据
     * @param [isDeleteId] 是否删除当前回调，删除后后面的回调将不会执行
     */
    fun callback(cbId: String, data: CallbackBean, isDeleteId: Boolean)

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [callback] 回调
     */
    fun callJsFunction(function: String, callback: JsCallback)


    /**
     * 调用Js代码
     * @param [function] js的方法
     */
    fun callJsFunction(function: String)

    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [data] 传递给js的数据
     * @param [callback] 回调
     */
    fun callJsFunction(function: String, data: String, callback: JsCallback)


    /**
     * 调用Js代码
     * @param [function] js的方法
     * @param [data] 传递给js的数据
     */
    fun callJsFunction(function: String, data: String)

    /**
     * Js调用原生的名称
     */
    fun jsCallName(): String {
        return "JsBridge"
    }


    /**
     * 获取js的Window，可不用重写，如果遇到window对象不对可以使用这个重新赋值window
     */
    fun getWindow(): String {
        return "window"
    }

}