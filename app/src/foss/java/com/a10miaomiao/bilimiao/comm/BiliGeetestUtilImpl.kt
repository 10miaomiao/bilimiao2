package com.a10miaomiao.bilimiao.comm

import android.app.Activity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.coroutineScope
import com.a10miaomiao.bilimiao.activity.GeetestValidatorActivity
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import kotlinx.coroutines.launch
import org.json.JSONObject

class BiliGeetestUtilImpl(
    val activity: Activity,
    val lifecycle: Lifecycle,
) : BiliGeetestUtil, DefaultLifecycleObserver {

    private val TAG = "BiliGeetestUtilImpl"

    override fun startCustomFlow(gtCallBack: BiliGeetestUtil.GTCallBack) {
        GeetestValidatorActivity.openGeetestValidatorActivity(
            activity = activity,
            callback = object : GeetestValidatorActivity.CallBack {
                override fun onGTResult(result: BiliGeetestUtil.GT3ResultBean) {
                    lifecycle.coroutineScope.launch {
                        gtCallBack.onGTDialogResult(result)
                    }
                }

                override suspend fun getGTApiJson(): JSONObject? {
                   return gtCallBack.getGTApiJson()
                }

            }
        )
    }

}