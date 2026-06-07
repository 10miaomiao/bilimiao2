package cn.a10miaomiao.bilimiao.compose.common.auth

import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.json.JSONObject

class GeetestVerifierAndroid(
    private val geetestUtil: BiliGeetestUtil,
) : GeetestVerifier {

    override fun startVerification(callback: GeetestCallback) {
        geetestUtil.startCustomFlow(object : BiliGeetestUtil.GTCallBack {
            override suspend fun onGTDialogResult(result: BiliGeetestUtil.GT3ResultBean): Boolean {
                return callback.onResult(
                    GeetestResult(
                        geetest_challenge = result.geetest_challenge,
                        geetest_seccode = result.geetest_seccode,
                        geetest_validate = result.geetest_validate,
                    )
                )
            }

            override suspend fun getGTApiJson(): JSONObject? {
                val json = callback.getApiJson() ?: return null
                return JSONObject(json.toString())
            }
        })
    }
}
