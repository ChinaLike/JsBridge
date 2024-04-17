package com.core.bridge.callback

/**
 *
 * @author like
 * @date 2024/1/4 20:47
 */
class Callback(private val callback: ICallback, private val cbId: String) {

    /**
     * 回调
     * @param [code] 错误码
     * @param [message] 消息
     * @param [data] 数据
     * @param [deleteCb] 删除回调
     */
    fun callback(code: Int, message: String, data: Any?, deleteCb: Boolean) {
        callback.callback(cbId, CallbackBean(code, message, data), deleteCb)
    }

    /**
     * 回调
     * @param [code] 错误码
     * @param [message] 消息
     * @param [data] 数据
     */
    fun callback(code: Int, message: String, data: Any?) {
        callback(code, message, data, false)
    }

    /**
     * 回调
     * @param [data] 数据
     * @param [deleteCb] 删除回调
     */
    fun success(data: Any?, deleteCb: Boolean) {
        callback(0, "成功", data, deleteCb)
    }

    /**
     * 回调
     * @param [data] 数据
     */
    fun success(data: Any?) {
        success(data, false)
    }

}