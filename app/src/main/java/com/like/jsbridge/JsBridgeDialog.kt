package com.like.jsbridge

import android.content.Context
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AlertDialog
import com.alibaba.fastjson.JSON
import com.core.web.Callback
import com.core.web.base.IJavascriptInterface

/**
 *
 * @author like
 * @date 6/1/21 5:28 PM
 */
class JsBridgeDialog(private val context: Context): IJavascriptInterface {

    @JavascriptInterface
    fun nativeDialog(params: String, callback: Callback) {
        val bean = JSON.parseObject(params, DialogBean::class.java)

        val dialog = AlertDialog.Builder(context).apply {
            setTitle(bean.title)
            setMessage(bean.content)
            setPositiveButton(bean.confirmText) { dialog, which ->
                dialog.dismiss()
                callback.success(message = "成功",data = bean.confirmText)
            }

            setNegativeButton(bean.cancelText) { dialog, which ->
                dialog.dismiss()
                callback.error(1,message = "失败", data = bean.cancelText)
            }
        }

        dialog.show()
    }

}