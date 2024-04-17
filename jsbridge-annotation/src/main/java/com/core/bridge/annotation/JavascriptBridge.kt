package com.core.bridge.annotation

/**
 * @author like
 * @date 2023/12/28 18:06
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class JavascriptBridge(val group: String)