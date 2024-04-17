package com.core.bridge.processor

import com.core.bridge.create.FileCreateManager
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 *
 * @author like
 * @date 2023/12/28 21:41
 */
abstract class BaseProcessor: AbstractProcessor() {

    protected var mFiler: Filer? = null

    protected var types:Types? = null

    protected var elementUtils: Elements? = null

    protected var fileCreateManager: FileCreateManager? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        mFiler = processingEnv?.filer
        types = processingEnv?.typeUtils
        elementUtils = processingEnv?.elementUtils
        if (mFiler != null && elementUtils != null){
            fileCreateManager = FileCreateManager(mFiler!!,elementUtils!!)
        }
    }

}