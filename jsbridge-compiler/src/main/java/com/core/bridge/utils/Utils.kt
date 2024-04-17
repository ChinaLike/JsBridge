package com.core.bridge.utils

import com.core.bridge.Constants
import com.core.bridge.callback.Callback
import com.squareup.javapoet.TypeName
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements

/**
 *
 * @author like
 * @date 2024/1/23 16:12
 */
object Utils {

    /**
     * 是否有 JavascriptInterface 注解
     */
    fun isBridgeAnnotation(annotation: String): Boolean {
        return annotation.contains(Consts.ANNOTATION_JAVASCRIPT_INTERFACE)
    }

    /**
     * 是否有 JavascriptInterface 注解
     */
    fun isBridgeAnnotation(element: ExecutableElement?): Boolean {
        var isBridgeAnnotation: Boolean = false
        element?.annotationMirrors?.forEach { annotationMirror ->
            if (isBridgeAnnotation(annotationMirror.toString())) {
                isBridgeAnnotation = true
            }
        }
        return isBridgeAnnotation
    }

    /**
     * 是否包含Callback参数
     */
    fun isContainerCallback(elementUtils: Elements?): String? {
        return elementUtils?.getTypeElement(Callback::class.java.name)?.toString()
    }

    fun isVoid(typeMirror: TypeMirror?): Boolean {
        return TypeName.get(typeMirror) == TypeName.VOID
    }

    /**
     * 获取别名
     */
    fun getAlias(className: String, method: String): String {
        val clazzName = className.replace(".", Constants.SEPARATOR)
        return Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + clazzName + Constants.SEPARATOR + method
    }

    /**
     * 获取文件名称
     */
    fun getFileName(group: String): String {
        return Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + Constants.FILE_GROUP + Constants.SEPARATOR + group
    }

    /**
     * 获取组名称
     */
    fun getGroupFileName(): String {
        return Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + "Group" + Constants.SEPARATOR + System.currentTimeMillis()
    }

    /**
     * 获取方法文件名称
     */
    fun getMethodFileName(group: String):String{
        return Constants.PREFIX_FILE_NAME + Constants.SEPARATOR + Constants.FILE_METHOD + Constants.SEPARATOR + group
    }

}