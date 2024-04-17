package com.core.bridge.model

import com.core.bridge.callback.Callback
import com.core.bridge.enums.BridgeType
import java.lang.reflect.Type
import javax.lang.model.element.VariableElement
import javax.lang.model.type.TypeMirror

/**
 *
 * @author like
 * @date 2024/1/4 20:13
 */
data class BridgeMeta(
    /**
     * 方法类型
     */
    val type: BridgeType,
    /**
     * 所属类
     */
    val destination: String,

    /**
     * 方法名称
     */
    val methodName: String,

    /**
     * 方法别名
     */
    val alias: String,

    /**
     * 是否有返回
     */
    val isReturn: Boolean
) {

    companion object{

        @JvmStatic
        fun build(type: BridgeType,destination: String,methodName: String,alias: String,isReturn: Boolean,paramsClassNameList:List<String>):BridgeMeta{
            return BridgeMeta(type, destination, methodName, alias, isReturn).apply {
                parametersClassNameList.clear()
                parametersClassNameList.addAll(paramsClassNameList)
            }
        }

    }

    /**
     * 参数
     */
    val parameters: MutableList<VariableElement> = mutableListOf()

    /**
     * 参数名称
     */
    val parametersClassNameList:MutableList<String> = mutableListOf()

    /**
     * 返回类型
     */
    var returnsType: TypeMirror? = null

    /**
     * 校验是否传参，而不是回调值
     */
    fun verifyParams(): Boolean {
        if (parametersClassNameList.size > 0) {
            return parametersClassNameList[0] != Callback::class.java.name
        }
        return false
    }

}