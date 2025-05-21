package com.a10miaomiao.bilimiao.comm

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.coroutineScope
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class BiliGeetestUtilImpl(
    val activity: Activity,
    val lifecycle: Lifecycle,
) : BiliGeetestUtil, DefaultLifecycleObserver {

    private val TAG = "BiliGeetestUtilImpl"

    private var gtCallBack: BiliGeetestUtil.GTCallBack? = null

//    var recaptchaToken = ""

    private val gt3Listener = object : GT3Listener() {
        /**
         * 验证码加载完成
         * @param duration 加载时间和版本等信息，为json格式
         */
        override fun onDialogReady(duration: String) {
            Log.e(TAG, "GT3BaseListener-->onDialogReady-->$duration")
        }

        /**
         * 图形验证结果回调
         * @param code 1为正常 0为失败
         */
        override fun onReceiveCaptchaCode(code: Int) {
            Log.e(TAG, "GT3BaseListener-->onReceiveCaptchaCode-->$code")
        }

        /**
         * 自定义api2回调
         * @param result，api2请求上传参数
         */
        override fun onDialogResult(result: String) {
            lifecycle.coroutineScope.launch(Dispatchers.Main) {
                try {
                    val resultBean = MiaoJson.fromJson<BiliGeetestUtil.GT3ResultBean>(result)
                    gtCallBack?.onGTDialogResult(resultBean)
                    gt3GeetestUtils.showSuccessDialog()
                } catch (e: Exception) {
                    Toast.makeText(activity, "验证回调错误：${e.message ?: e.toString()}", Toast.LENGTH_SHORT)
                        .show()
                    gt3GeetestUtils.showFailedDialog()
                }
            }
            // 开启自定义api2逻辑
        }

        /**
         * 统计信息，参考接入文档
         * @param result
         */
        override fun onStatistics(result: String) {
            Log.e(TAG, "GT3BaseListener-->onStatistics-->$result")
        }

        /**
         * 验证码被关闭
         * @param num 1 点击验证码的关闭按钮来关闭验证码, 2 点击屏幕关闭验证码, 3 点击返回键关闭验证码
         */
        override fun onClosed(num: Int) {
        }

        /**
         * 验证成功回调
         * @param result
         */
        override fun onSuccess(result: String) {
            Log.e(TAG, "GT3BaseListener-->onSuccess-->$result")
        }

        /**
         * 验证失败回调
         * @param errorBean 版本号，错误码，错误描述等信息
         */
        override fun onFailed(errorBean: GT3ErrorBean) {
            Toast.makeText(activity, "验证失败：${errorBean.errorDesc}", Toast.LENGTH_SHORT)
                .show()
            gt3GeetestUtils.dismissGeetestDialog()
            Log.e(TAG, "GT3BaseListener-->onFailed-->$errorBean")
        }

        /**
         * 自定义api1回调
         */
        override fun onButtonClick() {
            lifecycle.coroutineScope.launch(Dispatchers.Main) {
                try {
                    val api1Json = gtCallBack?.getGTApiJson()
                    if (api1Json == null) {
                        gt3GeetestUtils.dismissGeetestDialog()
                    } else {
                        gt3ConfigBean.api1Json = api1Json
                        // 继续验证
                        gt3GeetestUtils.getGeetest()
                    }
                } catch (e: Exception) {
                    Toast.makeText(activity, "网络错误：$e", Toast.LENGTH_SHORT)
                        .show()
                    gt3GeetestUtils.dismissGeetestDialog()
                    e.printStackTrace()
                }
            }
        }
    }

    // 配置 GT3ConfigBean 文件, 也可在调用 startCustomFlow 方法前处理
    private val gt3ConfigBean = GT3ConfigBean()

    private val gt3GeetestUtils = GT3GeetestUtils(activity)

    init {
        gt3ConfigBean.run {
            pattern = 1;
            // 设置点击灰色区域是否消失，默认不消息
            isCanceledOnTouchOutside = false;
            // 设置语言，如果为null则使用系统默认语言
            lang = null;
            // 设置加载webview超时时间，单位毫秒，默认10000，仅且webview加载静态文件超时，不包括之前的http请求
            timeout = 10000;
            // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
            webviewTimeout = 10000;
            listener = gt3Listener
        }
        gt3GeetestUtils.init(gt3ConfigBean)
        lifecycle.addObserver(this)
    }

    override fun startCustomFlow(callBack: BiliGeetestUtil.GTCallBack) {
        this.gtCallBack = callBack
        // 开启验证
        gt3GeetestUtils.startCustomFlow()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        gt3GeetestUtils.destory()
    }
}