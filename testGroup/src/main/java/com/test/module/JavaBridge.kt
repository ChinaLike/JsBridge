package com.test.module

import android.webkit.JavascriptInterface
import com.core.bridge.annotation.JavascriptBridge

/**
 *
 * @author like
 * @date 2024/2/6 15:30
 */
@JavascriptBridge("test2")
class JavaBridge {

    @JavascriptInterface
    fun testGroup(){

    }

}