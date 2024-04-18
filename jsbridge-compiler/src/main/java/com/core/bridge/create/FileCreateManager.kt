package com.core.bridge.create

import com.core.bridge.Constants
import com.core.bridge.callback.Callback
import com.core.bridge.callback.ICallback
import com.core.bridge.callback.JsCallbackNative
import com.core.bridge.enums.BridgeType
import com.core.bridge.interfaces.IBridgeMethod
import com.core.bridge.interfaces.IInjectBridge
import com.core.bridge.model.BridgeMeta
import com.core.bridge.utils.Consts
import com.core.bridge.utils.Utils
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.CodeBlock
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.MethodSpec
import com.squareup.javapoet.ParameterSpec
import com.squareup.javapoet.TypeName
import com.squareup.javapoet.TypeSpec
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import javax.annotation.processing.Filer
import javax.lang.model.element.Modifier
import javax.lang.model.element.VariableElement
import javax.lang.model.util.Elements

/**
 *
 * @author like
 * @date 2023/12/28 23:23
 */
class FileCreateManager(private val mFiler: Filer, private val elementUtils: Elements) {

    private val stringTypeName = ClassName.get(String::class.java)

    private val objectTypeName = ClassName.get(Object::class.java)

    private val callbackTypeName = ClassName.get(Callback::class.java)

    /**
     * 写Java文件
     */
    fun fileWrite(map: MutableMap<String, MutableList<BridgeMeta>>) {
        val exceptionList: MutableList<TypeName> = mutableListOf()
        exceptionList.add(TypeName.get(NoSuchMethodException::class.java))
        exceptionList.add(TypeName.get(InvocationTargetException::class.java))
        exceptionList.add(TypeName.get(IllegalAccessException::class.java))
        //组+生成的类
        val groupBridge: MutableMap<String, String> = mutableMapOf()
        //根据包名生成方法
        map.forEach { (key, bridgeMetaList) ->
            val fileName = Utils.getFileName(key)
            val methodSpecList: MutableList<MethodSpec> = mutableListOf()
            bridgeMetaList.forEach {
                val methodSpec =
                    MethodSpec
                        .methodBuilder(if (it.type == BridgeType.SYNCHRONIZE) it.methodName else it.alias)
                        .addModifiers(Modifier.PUBLIC)
                        .addExceptions(exceptionList)
                        .addAnnotation(
                            ClassName.get(Consts.ANDROID_WEBKIT, Consts.JAVASCRIPT_INTERFACE)
                        )
                        .addParameters(generateMethodParameter(it.parameters))
                        .addCode(addMethodCodeBlock(it))
                        .returns(TypeName.get(it.returnsType))
                        .build()
                methodSpecList.add(methodSpec)
            }
            JavaFile.builder(
                Constants.PACKAGE_OF_GENERATE_FILE,
                TypeSpec.classBuilder(fileName)
                    .addModifiers(Modifier.PUBLIC)
                    .addField(ICallback::class.java, "callback", Modifier.PRIVATE, Modifier.FINAL)
                    .addField(
                        IInjectBridge::class.java,
                        "injectBridge",
                        Modifier.PRIVATE,
                        Modifier.FINAL
                    )
                    .addMethod(createConstructorParams())
                    .addMethods(methodSpecList)
                    .addMethod(generateJsCallbackNativeMethod())
                    .addMethod(generateExecuteObject())
                    .build()
            )
                .build().writeTo(mFiler)

            //生成方法
            generateMethodGroup(key, bridgeMetaList)

            groupBridge[key] = Constants.PACKAGE_OF_GENERATE_FILE + "." + fileName
        }
    }

    /**
     * 创建构造参数
     */
    private fun createConstructorParams(): MethodSpec {
        return MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ICallback::class.java, "callback")
            .addParameter(IInjectBridge::class.java, "injectBridge")
            .addStatement("this.\$N = \$N", "callback", "callback")
            .addStatement("this.\$N = \$N", "injectBridge", "injectBridge")
            .build()
    }

    /**
     * 生成方法的参数
     */
    private fun generateMethodParameter(parameters: MutableList<out VariableElement>?): MutableList<ParameterSpec> {
        val parameterList: MutableList<ParameterSpec> = mutableListOf()
        var addCbId: Int = 0
        parameters?.forEach {
            val callbackTypeMirror = elementUtils.getTypeElement(Consts.LIBRARY_CALLBACK)
            val typeMirror = it.asType()
            if (callbackTypeMirror.toString() == typeMirror.toString()) {
                if (addCbId == 2) {
                    throw IllegalArgumentException("最多支持2个回调,第1个是成功回调，第2个是失败回调")
                }
                if (addCbId == 0) {
                    val successSpec = ParameterSpec.builder(stringTypeName, "cbId").build()
                    parameterList.add(successSpec)
                } else if (addCbId == 1) {
                    val failSpec = ParameterSpec.builder(stringTypeName, "failCbId").build()
                    parameterList.add(failSpec)
                }
                addCbId++
            } else {
                val typeName = TypeName.get(typeMirror)
                val spec = ParameterSpec.builder(typeName, it.simpleName.toString()).build()
                parameterList.add(spec)
            }
        }
        return parameterList
    }

    /**
     * 添加方法体
     */
    private fun addMethodCodeBlock(bridgeMeta: BridgeMeta): CodeBlock {
        if (bridgeMeta.parameters.size > 3) {
            throw IllegalArgumentException("${bridgeMeta.methodName}的方法参数不能超过3个")
        }
        val builder = CodeBlock.builder()
        builder.add("\$T obj = getObjectInstance(\$S);\n", Any::class.java, bridgeMeta.destination)
        when (bridgeMeta.parameters.size) {
            0 -> {
                builder.add(
                    "\$T method = obj.getClass().getMethod(\$S);\n",
                    Method::class.java,
                    bridgeMeta.methodName
                )
                builder.add("Object result = method.invoke(obj);\n")
                if (bridgeMeta.returnsType?.toString() != TypeName.VOID.toString()) {
                    builder.add("return (${bridgeMeta.returnsType?.toString()}) result;\n")
                }
            }

            1 -> {
                val params1VariableElement = bridgeMeta.parameters[0]
                val params1TypeMirror = params1VariableElement.asType()
                builder.add(
                    "\$T method = obj.getClass().getMethod(\$S,\$T.class);\n",
                    Method::class.java,
                    bridgeMeta.methodName,
                    params1TypeMirror
                )
                if (params1TypeMirror.toString() == callbackTypeName.toString()) {
                    builder.add(
                        "Object result = method.invoke(obj,new \$T(this.callback,cbId));\n",
                        Callback::class.java
                    )
                } else {
                    builder.add("Object result = method.invoke(obj,${params1VariableElement.simpleName});\n")
                }
                if (bridgeMeta.returnsType?.toString() != TypeName.VOID.toString()) {
                    builder.add("return (${bridgeMeta.returnsType?.toString()})result;\n")
                }

            }

            2 -> {
                val params1VariableElement = bridgeMeta.parameters[0]
                val params1TypeMirror = params1VariableElement.asType()
                val params2VariableElement = bridgeMeta.parameters[1]
                val params2TypeMirror = params1VariableElement.asType()
                builder.add(
                    "\$T method = obj.getClass().getMethod(\$S,\$T.class,\$T.class);\n",
                    Method::class.java,
                    bridgeMeta.methodName,
                    params1TypeMirror,
                    params2TypeMirror
                )
                if (params1TypeMirror.toString() != callbackTypeName.toString()) {
                    builder.add(
                        "Object result = method.invoke(obj,${params1VariableElement.simpleName},new \$T(this.callback,cbId));\n",
                        Callback::class.java
                    )
                } else {
                    builder.add(
                        "Object result = method.invoke(obj,new \$T(this.callback,cbId),new \$T(this.callback,failCbId));\n",
                        Callback::class.java,
                        Callback::class.java
                    )
                }
                if (bridgeMeta.returnsType?.toString() != TypeName.VOID.toString()) {
                    builder.add("return (${bridgeMeta.returnsType?.toString()}) result;\n")
                }
            }

            3 -> {
                val params1VariableElement = bridgeMeta.parameters[0]
                val params1TypeMirror = params1VariableElement.asType()
                val params2VariableElement = bridgeMeta.parameters[1]
                val params2TypeMirror = params1VariableElement.asType()
                val params3VariableElement = bridgeMeta.parameters[2]
                val params3TypeMirror = params1VariableElement.asType()
                builder.add(
                    "\$T method = obj.getClass().getMethod(\$S,\$T.class,\$T.class,\$T.class);\n",
                    Method::class.java,
                    bridgeMeta.methodName,
                    params1TypeMirror,
                    params2TypeMirror,
                    params3TypeMirror
                )
                builder.add(
                    "Object result = method.invoke(obj,${params1VariableElement.simpleName},new \$T(this.callback,cbId),new \$T(this.callback,failCbId));\n",
                    Callback::class.java,
                    Callback::class.java
                )
                if (bridgeMeta.returnsType?.toString() != TypeName.VOID.toString()) {
                    builder.add("return (${bridgeMeta.returnsType?.toString()}) result;\n")
                }
            }
        }

        return builder.build()
    }

    /**
     * 生成Js回调给原生的方法
     */
    private fun generateJsCallbackNativeMethod(): MethodSpec {
        val codeBlock = CodeBlock.builder()
            .addStatement("if (cbId.isEmpty()) return")
            .addStatement(
                "\$T jsCallbackNative = callback.getJavascriptCallbackNativeId(cbId,deleteCbId)",
                JsCallbackNative::class.java
            )
            .beginControlFlow("if (jsCallbackNative != null)")
            .addStatement("jsCallbackNative.onJsCallback(data)")
            .endControlFlow()
            .build()
        return MethodSpec
            .methodBuilder("jsCallbackDispatch")
            .addModifiers(Modifier.PUBLIC)
            .addAnnotation(ClassName.get(Consts.ANDROID_WEBKIT, Consts.JAVASCRIPT_INTERFACE))
            .addParameter(String::class.java, "cbId")
            .addParameter(String::class.java, "data")
            .addParameter(Boolean::class.java, "deleteCbId")
            .addCode(codeBlock)
            .build()
    }

    /**
     * 在数组中找到指定类名的实例
     */
    private fun generateExecuteObject(): MethodSpec {
        val codeBlock = CodeBlock.builder()
            .add(
                "\$T object = this.injectBridge.getInjectBridgeMap().get(className);\n",
                Any::class.java,
            )
            .add("if (object == null) {\n")
            .add(
                "\tthrow new \$T(\"请先调用BaseWebView的addJavascriptInterface单参数方法\");\n",
                NullPointerException::class.java
            )
            .add("}\n")
            .add("return object;\n")
            .build()
        return MethodSpec
            .methodBuilder("getObjectInstance")
            .addModifiers(Modifier.PRIVATE)
            .addParameter(ParameterSpec.builder(stringTypeName, "className").build())
            .addCode(codeBlock)
            .returns(objectTypeName)
            .build()
    }

    /**
     * 按照组把方法归类
     */
    private fun generateMethodGroup(group: String, bridgeMetaList: MutableList<BridgeMeta>) {
        if (group.isNullOrEmpty() || bridgeMetaList.size == 0) {
            return
        }
        val constructorBuilder = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
            .addStatement(
                "list = new \$T<\$T>()",
                ArrayList::class.java,
                BridgeMeta::class.java
            )
            .build()
        val codeBlock = CodeBlock.builder()
        codeBlock.addStatement("list.clear()")
        bridgeMetaList.forEach {
            val mapBodyBuilder = StringBuilder()
            it.parameters.forEach { variableElement ->
                mapBodyBuilder.append("add(\"${variableElement.asType().toString()}\");")
            }
            codeBlock.addStatement(
                "list.add(\$T.build(\$T.${it.type},\$S,\$S,\$S,${it.isReturn},new \$T<String>(){{${mapBodyBuilder}}}))",
                BridgeMeta::class.java,
                BridgeType::class.java,
                it.destination,
                it.methodName,
                it.alias,
                ArrayList::class.java
            )
        }
        codeBlock.addStatement("return list", ArrayList::class.java)
        val methodBuilder = MethodSpec.methodBuilder("loadMethod")
            .addAnnotation(Override::class.java)
            .addModifiers(Modifier.PUBLIC)
            .addCode(codeBlock.build())
            .returns(List::class.java)
            .build()
        JavaFile.builder(
            Constants.PACKAGE_OF_GENERATE_FILE,
            TypeSpec.classBuilder(Utils.getMethodFileName(group))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(IBridgeMethod::class.java)
                .addField(List::class.java, "list", Modifier.PRIVATE)
                .addMethod(constructorBuilder)
                .addMethod(methodBuilder)
                .build()
        )
            .build().writeTo(mFiler)
    }
}