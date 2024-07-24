package com.a10miaomiao.bilimiao.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import androidx.lifecycle.lifecycleScope
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.utils.BiliGeetestUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.appbar.MaterialToolbar
import com.king.zxing.CaptureActivity
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.kodein.di.DI
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.setContentView
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInScrollView

class GeetestValidatorActivity : AppCompatActivity() {

    companion object {

        private var tempCallback: CallBack? = null

        fun openGeetestValidatorActivity(activity: Activity, callback: CallBack) {
            tempCallback = callback
            val intent = Intent(activity, GeetestValidatorActivity::class.java)
            activity.startActivity(intent)
        }
    }

    private val di: DI = DI.lazy {}

    private var mCallback: CallBack? = null

    private val themeDelegate by lazy {
        ThemeDelegate(this@GeetestValidatorActivity, di)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        mCallback = tempCallback
        themeDelegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
        val jsBridge = JsBridge(this)
        val ui = GeetestValidatorUi(this, jsBridge)
        setContentView(ui)
        getGTApi(ui.webView)
    }

    private fun getGTApi(
        webView: WebView
    ) = lifecycleScope.launch {
        mCallback?.getGTApiJson()?.let {
            val gt = it.getString("gt")
            val challenge = it.getString("challenge")
            val url = "file:///android_asset/geetest-validator/index.html?gt=${gt}&challenge=${challenge}"
            webView.loadUrl(url)
        }
    }

    fun onGTResult(
        gt3Result: BiliGeetestUtil.GT3ResultBean
    ) {
        mCallback?.onGTResult(gt3Result)
        finish()
    }

    private class JsBridge(
        val activity: GeetestValidatorActivity
    ) {
        @JavascriptInterface
        fun postMessage(challenge: String, validate: String, seccode: String) {
            activity.onGTResult(
                BiliGeetestUtil.GT3ResultBean(
                    geetest_challenge = challenge,
                    geetest_validate = validate,
                    geetest_seccode = seccode,
                )
            )
        }
        @JavascriptInterface
        fun close() {
            activity.finish()
        }
    }

    @OptIn(InternalSplittiesApi::class)
    private class GeetestValidatorUi(
        val activity: GeetestValidatorActivity,
        val jsBridge: JsBridge,
    ) : Ui {
        override val ctx = activity

        val toolBar = view<MaterialToolbar>(View.generateViewId()) {
            clipToPadding = true
            fitsSystemWindows = true
            setTitle(R.string.geetest_validator)
            setNavigationOnClickListener {
                activity.onBackPressed()
            }
        }

        val webView = view<WebView> {
            settings.apply {
                javaScriptEnabled = true
            }
            addJavascriptInterface(jsBridge, "JsBridge")
        }

        override val root: View = verticalLayout {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            fitsSystemWindows = true

            addView(toolBar, lParams(matchParent, wrapContent))

            addView(webView, lParams(matchParent, matchParent) {
                weight = 1f
            })

        }
    }

    interface CallBack {
        fun onGTResult(
            result: BiliGeetestUtil.GT3ResultBean,
        )
        suspend fun getGTApiJson(): JSONObject?
    }
}