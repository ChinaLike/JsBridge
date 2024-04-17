package com.core

import android.content.Context
import com.core.bridge.Constants
import com.core.util.ClassUtils

/**
 *
 * @author like
 * @date 2024/2/7 17:55
 */
class JSBridge private constructor() {

    private var init: Boolean = false

    private val clazzList: MutableList<String> = mutableListOf()

    companion object {

        @Volatile
        private var instance: JSBridge? = null

        @JvmStatic
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: JSBridge().also { instance = it }
        }

    }

    fun init(applicationContext: Context) {
        if (init) {
            return
        }
        clazzList.clear()
        val list = ClassUtils.getFileNameByPackageName(
            applicationContext,
            Constants.PACKAGE_OF_GENERATE_FILE
        )
        clazzList.addAll(list)
        init = true
    }

    /**
     * 获取所有类
     */
    fun getClazzList():MutableList<String>{
        if (!init){
            throw RuntimeException("请先调用init()初始化")
        }
        return clazzList
    }

}