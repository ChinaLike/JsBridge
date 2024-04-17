package com.like.jsbridge

import android.app.Application
import com.core.JSBridge

/**
 *
 * @author like
 * @date 2024/2/8 09:54
 */
class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
        JSBridge.getInstance().init(this)
    }
}