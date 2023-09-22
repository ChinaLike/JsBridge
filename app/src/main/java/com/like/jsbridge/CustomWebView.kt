package com.like.jsbridge

import android.content.Context
import android.util.AttributeSet
import com.core.web.JsBridgeWebView

/**
 *
 * @author like
 * @date 2023/9/18 13:35
 */
class CustomWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) :
    JsBridgeWebView(context, attrs, defStyle) {

    /**
     * 同步回调名称
     */
    fun jsCallSynchronizeCallbackName(): String {
        return jsCallName() + "Synchronize"
    }
}