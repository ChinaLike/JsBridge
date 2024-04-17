package com.core.bridge.interfaces

import com.core.bridge.model.BridgeMeta

/**
 *
 * @author like
 * @date 2024/2/7 17:39
 */
interface IBridgeMethod {

    fun loadMethod(): List<BridgeMeta>

}