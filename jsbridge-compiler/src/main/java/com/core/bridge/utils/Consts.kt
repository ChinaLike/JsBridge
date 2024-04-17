package com.core.bridge.utils

/**
 * @author like
 * @date 2023/12/28 21:33
 */
object Consts {

    private const val ANNOTATION_PACKAGE = "com.core.bridge"

    const val ANNOTATION_JS_BRIDGE = "$ANNOTATION_PACKAGE.annotation.JavascriptBridge"

    const val ANDROID_WEBKIT = "android.webkit"

    const val JAVASCRIPT_INTERFACE = "JavascriptInterface"

    const val ANNOTATION_JAVASCRIPT_INTERFACE = "$ANDROID_WEBKIT.$JAVASCRIPT_INTERFACE"

    private const val LIBRARY_PACKAGE = "com.core.bridge"

    const val LIBRARY_CALLBACK = "$ANNOTATION_PACKAGE.callback.Callback"


}