package com.core.util

import java.lang.reflect.Method

/**
 * 反射工具类
 * @author like
 * @date 5/25/21 9:46 AM
 */
object ReflectUtil {


    /**
     * 获取所有方法
     */
    fun getMethods(`object`: Any): Array<Method> {
        return `object`.javaClass.declaredMethods
    }

    /**
     * 获取类中指定注解的方法字符串名称
     * @param [`object`] 对象名
     * @param [annotationName] 注解名称
     */
    fun getMethods(`object`: Any, annotationName: String): MutableList<Method> {
        val methods = getMethods(`object`)
        var methodList: MutableList<Method> = mutableListOf()
        methods?.forEach { method ->
            method.annotations?.forEach { annotation ->
                if (annotation.annotationClass.simpleName == annotationName) {
                    methodList.add(method)
                }
            }
        }
        return methodList
    }

}