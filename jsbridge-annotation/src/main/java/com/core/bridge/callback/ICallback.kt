package com.core.bridge.callback

/**
 *
 * @author like
 * @date 2024/1/4 20:42
 */
interface ICallback {

    /**
     * 回调
     * @param [cbId] 回调ID
     * @param [data] 回调数据
     * @param [deleteCb] 是否删除回调
     */
    fun callback(cbId:String,data: CallbackBean,deleteCb:Boolean = false)

    /**
     * 获取Js回调给原生的接口
     * @param [cbId] 回调Id
     * @param [deleteCbId] 是否删除回调Id
     */
    fun getJavascriptCallbackNativeId(cbId: String,deleteCbId:Boolean):JsCallbackNative?

}