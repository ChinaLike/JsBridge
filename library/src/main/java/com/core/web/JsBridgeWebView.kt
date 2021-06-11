package com.core.web

import android.content.Context
import android.util.AttributeSet
import com.core.web.base.BaseWebView

/**
 * 桥梁WebView
 * @author like
 * @date 6/11/21 10:45 AM
 */
class JsBridgeWebView : BaseWebView {


    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

}