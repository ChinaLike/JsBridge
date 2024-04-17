package com.like.jsbridge

import com.google.gson.annotations.SerializedName

/**
 *
 * @author like
 * @date 2024/2/8 21:05
 */
data class CustomBean(
    @SerializedName("title") val title:String,
    @SerializedName("content") val content:String,
    @SerializedName("confirmText") val confirmText:String,
    @SerializedName("cancelText") val cancelText:String
)
