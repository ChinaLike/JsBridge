package com.core.bridge.interfaces

/**
 *
 * @author like
 * @date 2024/2/7 15:02
 */
interface IInjectBridge {

    /**
     * 获取注入的桥梁类
     */
    fun getInjectBridgeMap(): MutableMap<String, Any>

}