package com.core.bridge.model

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 *
 * @author like
 * @date 2024/1/5 23:17
 */
class TypeWrapper<T> {

    private var type: Type

    constructor() {
        val superClass: Type = javaClass.genericSuperclass
        type = (superClass as ParameterizedType).actualTypeArguments[0]
    }

    fun getType(): Type {
        return type
    }
}