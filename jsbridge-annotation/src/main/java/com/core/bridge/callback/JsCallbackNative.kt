package com.core.bridge.callback

/**
 * Js 回调数据给原生
 * @author like
 * @date 2024/1/8 22:33
 */
interface JsCallbackNative {

    /**
     * js回调给原生
     */
    fun onJsCallback(callback: Any)

}