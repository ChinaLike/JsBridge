package com.core.web

import com.core.web.base.BaseWebView
import org.jetbrains.annotations.NotNull

/**
 * 原生异步回调
 * @author like
 * @date 5/26/21 5:01 PM
 */
class Callback(private val webView: BaseWebView, private val cbId: String) {

    /**
     * 成功的回调
     * @param [isDelete] 是否删除回调，true 删除后本次回调结束 ， false 可以再次回调
     * @param [message] 回调消息
     * @param [data] 回调数据
     */
    @JvmOverloads
    fun success(
        message: String = "成功",
        data: Any? = null,
        isDelete: Boolean = false
    ) {
        success(CallbackBean(0, message, data), isDelete)
    }

    /**
     * 成功的回调
     * @param [successData] 成功回调的数据
     * @param [isDelete] 是否删除回调
     */
    @JvmOverloads
    fun success(@NotNull  successData: CallbackBean, isDelete: Boolean = false) {
        webView.callback(cbId, successData, isDelete)
    }

    /**
     * 失败的回调
     * @param [isDelete] 是否删除回调，true 删除后本次回调结束 ， false 可以再次回调
     * @param [code] 状态码
     * @param [message] 回调消息
     * @param [data] 回调数据
     */
    @JvmOverloads
    fun error(
        code: Int,
        message: String,
        data: Any? = null,
        isDelete: Boolean = false
    ) {
        error(CallbackBean(code, message, data), isDelete)
    }

    /**
     * 失败的回调
     * @param [isDelete] 是否删除回调，true 删除后本次回调结束 ， false 可以再次回调
     * @param [errorData] 失败的回调
     */
    @JvmOverloads
    fun error(@NotNull errorData:CallbackBean,isDelete: Boolean = false){
        webView.callback(cbId, errorData, isDelete)
    }
}