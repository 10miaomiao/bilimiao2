package cn.a10miaomiao.bilimiao.compose.common

import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.fragment.app.Fragment
import cn.a10miaomiao.bilimiao.compose.common.navigation.BilibiliNavigation
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import com.a10miaomiao.bilimiao.comm.BilimiaoCommApp
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeToSequence
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class BiliJsBridge(
    val fragment: Fragment,
    val pageNavigation: PageNavigation,
    val webView: WebView,
//    val closeBrowser: () -> Unit,
) {
    private val activity get() = fragment.requireActivity()

    private val allSupportMethod = listOf<String>(
        "global.closeBrowser",
        "ui.setStatusBarMode",
//       "auth.checkBridgeEnable",
        "auth.getUserInfo",
//      "auth.getAccessToken",
//      "auth.getBaseInfo",
//      "auth.getAllBridge",
//      "auth.getTeenable",
//       "auth.getNetEnv",
        "auth.login",
        "ability.openScheme",
        "ability.currentThemeType",
        "view.goBack",
        "view.closeBrowser",
        "view.toast",
        "view.refresh",
        "view.setTitle",
//        "view.isLongScreen",
//        "route.login",
//        "route.editUserInfo",
//        "route.record",
//        "route.recommend",
//        "share.showShareWindow",
        "share.showShareMpcWindow",
//            "func.route",
//            "func.share",
//            "func.setShare",
//            "func.childrenOn",
//            "func.childrenOff",
//            "func.copy",
//            "func.cloud-editor.sync",
//            "func.creation-center.switchTabVisible",
//            "func.fixWindow",
//            "func.push.status",
//            "func.vipDraw.result",
//            "func.report.success",
    )

    @JavascriptInterface
    fun postMessage(eventString: String) {
        miaoLogger().d("postMessage" to eventString)
        val event = MiaoJson.fromJson<MessageEventInfo>(eventString)
        var result = ""
        when (event.method) {
            "ui.setStatusBarMode" -> {

            }
            "auth.getUserInfo" -> {

            }
            "global.getAllSupport" -> {
                result = "[${allSupportMethod.joinToString(",") { "\"$it\"" }}]"
            }
            "global.closeBrowser",
            "view.closeBrowser",
            "view.goBack" -> {
//                closeBrowser.invoke()
                activity.runOnUiThread {
                    pageNavigation.popBackStack()
                }
            }
            "view.refresh" -> {
                webView.reload()
            }
            "share.setShareContent" -> {
                activity.runOnUiThread {
                    PopTip.show("暂不支持分享操作")
                }
            }
            "share.showShareMpcWindow" -> {
                val defaultData = event.data.jsonObject["default"]?.jsonObject ?: return
                val title = defaultData["title"]?.jsonPrimitive?.content ?: ""
                val text = defaultData["text"]?.jsonPrimitive?.content ?: ""
                val url = defaultData["url"]?.jsonPrimitive?.content ?: ""
                activity.runOnUiThread {
                    val sendIntent = Intent(Intent.ACTION_SEND)
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "$title $url $text");
                    sendIntent.setType("text/plain")
                    activity.startActivity(sendIntent)
                }
            }
            "ability.openScheme" -> {
                val url = event.data.jsonObject["url"]?.jsonPrimitive?.content ?: "" ?: return
                activity.runOnUiThread {
                    val re = BilibiliNavigation.navigationTo(
                        pageNavigation,
                        url
                    )
                    if (!re) {
                        webView.loadUrl(url)
                    }
                }
            }
            "ability.currentThemeType" -> {
                result = """
                    {
                        type: 1
                    }
                    """.trimIndent()
            }
            "auth.login" -> {
                val loginInfo = BilimiaoCommApp.commApp.loginInfo
                if (loginInfo != null) {
                    // TODO: 刷新登录cookie
                    val onLoginCallbackId = event.data.jsonObject["onLoginCallbackId"]?.jsonPrimitive?.content
                    if (onLoginCallbackId != null) {
                        biliCallbackReceived(onLoginCallbackId, "{ state: 1 }")
                    }
                }
            }
        }
        activity.runOnUiThread {
            event.callback(result)
        }
    }

    fun MessageEventInfo.callback(
        result: String
    ) {
        val callbackId = data.jsonObject["callbackId"]?.jsonPrimitive?.content
        callbackId?.let {
            biliCallbackReceived(it, result)
        }
    }

    fun biliCallbackReceived(
        callbackId: String,
        data: String,
    ) {
        val javascript = """(function() {
                window.BiliJsBridge.biliInject.biliCallbackReceived($callbackId, $data)
            })()
            """.trimIndent()
        activity.runOnUiThread {
            webView.evaluateJavascript(javascript){ }
        }
    }

    @Serializable
    data class MessageEventInfo(
        val method: String,
        val data: JsonElement
    )
}