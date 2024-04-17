package com.core.bridge.processor

import com.core.bridge.annotation.JavascriptBridge
import com.core.bridge.create.FileCreateManager
import com.core.bridge.enums.BridgeType
import com.core.bridge.model.BridgeMeta
import com.core.bridge.utils.Consts
import com.core.bridge.utils.Utils
import com.google.auto.service.AutoService
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.annotation.processing.SupportedAnnotationTypes
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement


@AutoService(Processor::class)
@SupportedAnnotationTypes(value = [Consts.ANNOTATION_JS_BRIDGE])
class JsBridgeProcessor : BaseProcessor() {

    private val groupMap: MutableMap<String, MutableList<BridgeMeta>> = mutableMapOf()

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (annotations != null && annotations.size > 0) {
            roundEnv?.getElementsAnnotatedWith(JavascriptBridge::class.java)?.let(::parseJsBridge)
            return true
        }
        return false
    }

    /**
     * 解析桥梁数据
     */
    private fun parseJsBridge(elements: Set<out Element>) {
        groupMap.clear()
        elements.forEach {
            //判断是否是类
            if (it.kind == ElementKind.CLASS) {
                //获取注解参数
                val javascriptBridge = it.getAnnotation(JavascriptBridge::class.java)
                val group = javascriptBridge.group
                if (group.isEmpty()) {
                    throw NullPointerException("JavascriptBridge的group参数长度应大于0")
                }
                //获取包名+类名
                val name = it.asType().toString()
                val bridgeMetaList: MutableList<BridgeMeta> = mutableListOf()
                it.enclosedElements?.forEach { method ->
                    if (method.kind == ElementKind.METHOD) {
                        val executableElement = method as? ExecutableElement
                        if (executableElement != null && Utils.isBridgeAnnotation(executableElement)) {
                            val methodName = method.simpleName.toString()
                            val bridgeMeta = handleBridgeMeta(name, methodName, executableElement)
                            bridgeMetaList.add(bridgeMeta)
                        }
                    }
                }
                //处理组
                if (groupMap.contains(group)) {
                    groupMap[group]?.addAll(bridgeMetaList)
                } else {
                    groupMap[group] = bridgeMetaList
                }
            }
        }
        if (mFiler != null && elementUtils != null && groupMap.isNotEmpty()) {
            FileCreateManager(mFiler!!, elementUtils!!).fileWrite(groupMap)
        }
    }

    private fun handleBridgeMeta(
        className: String,
        methodName: String,
        element: ExecutableElement?
    ): BridgeMeta {
        var isContainCallback = false
        element?.parameters?.forEach { variableElement ->
            val callbackTypeMirror = Utils.isContainerCallback(elementUtils)
            val currTypeMirror = variableElement.asType().toString()
            if (callbackTypeMirror == currTypeMirror) {
                isContainCallback = true
            }
        }
        //返回
        val isReturn = Utils.isVoid(element?.returnType)
        val bridgeType = if (!isReturn && isContainCallback) {
            BridgeType.BOTH
        } else if (isReturn && isContainCallback) {
            BridgeType.ASYNCHRONOUS
        } else {
            BridgeType.SYNCHRONIZE
        }
        //别名
        val alias = Utils.getAlias(className, methodName)
        return BridgeMeta(bridgeType, className, methodName, alias, isReturn).apply {
            parameters.clear()
            element?.parameters?.let(parameters::addAll)
            //返回类型
            returnsType = element?.returnType
        }
    }

}