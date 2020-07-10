package com.a10miaomiao.bilimiao.ui.login

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.LoginInfo
import com.a10miaomiao.bilimiao.netword.MiaoHttp
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.web.WebFragment
import com.a10miaomiao.bilimiao.utils.BilibiliRouter
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.miaoandriod.MiaoLiveData
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.json.JSONObject
import java.lang.Exception

class H5LoginFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(): H5LoginFragment {
            val fragment = H5LoginFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    private val loading = MiaoLiveData(false)
    private var mWebView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(createUI().view)
    }


    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            if (url.contains("access_key=") && url.contains("mid=")) {
                try {
                    val accessKey = "access_key=(.*?)&".toRegex().find(url)!!.groupValues[1]
                    val mid = "mid=(.*?)&".toRegex().find(url)!!.groupValues[1]
                    val loginInfo = LoginInfo(
                            token_info = LoginInfo.TokenInfo(
                                    access_token = accessKey,
                                    mid = mid.toLong(),
                                    expires_in = 7200,
                                    refresh_token = ""
                            ),
                            status = 0,
                            sso = null,
                            cookie_info = null
                    )
                    Bilimiao.app.saveAuthInfo(loginInfo)
                    pop()
                    return false
                } catch (e: Exception) {
                    DebugMiao.log("获取登录参数时，发生错误")
                    e.printStackTrace()
                }
            }

            return BilibiliRouter.gotoUrl(context!!, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loading set true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            loading set false
            if (url == "https://passport.bilibili.com/ajax/miniLogin/redirect") {
                MiaoHttp.getString("https://passport.bilibili.com/login/app/third?appkey=27eb53fc9058f8c3&api=http%3A%2F%2Flink.acg.tv%2Fforum.php&sign=67ec798004373253d60114caaad89a8c")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ result ->
                            DebugMiao.log(result)
                            val json = JSONObject(result)
                            if (json.getInt("code") == 0) {
                                view.loadUrl(json.getJSONObject("data").getString("confirm_uri"))
                            } else {
                                context?.toast("登录失败，请重试");
                            }
                        }, { e ->
                            context?.toast("登录过程发生错误")
                            e.printStackTrace()
                        })
            } else if (url.contains("bilibili.com")) {
                val js = """javascript:(function() {
                        var parent = document.getElementsByTagName('head').item(0);
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = '#internationalHeader,.international-footer,.bili-footer,#cannot-check{display: none !important;}';
                        parent.appendChild(style);
                    })()
                """
                view.loadUrl(js)
            }
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {

    }

    override fun onBackPressedSupport(): Boolean {
        if (mWebView?.canGoBack() == true) {
            mWebView?.goBack()
            return true
        }
        return super.onBackPressedSupport()
    }

    private fun createUI() = UI {
        verticalLayout {
            headerView {
                title("网页登录")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            frameLayout {
                webView {
                    mWebView = this
                    backgroundColor = config.windowBackgroundColor
                    webViewClient = mWebViewClient
                    webChromeClient = mWebChromeClient
                    settings.apply {
                        javaScriptEnabled = true
                    }
                    loadUrl("https://passport.bilibili.com/ajax/miniLogin/minilogin")
                }.lparams(matchParent, matchParent)

                progressBar {
                    (+loading) { visibility = if (it) View.VISIBLE else View.GONE }
                }.lparams {
                    width = dip(64)
                    height = dip(64)
                    gravity = Gravity.CENTER
                }

            }.lparams(matchParent, matchParent)
        }
    }

    // DedeUserID=384046343&
    // DedeUserID__ckMd5=8b404bb89a9e86d7&
    // Expires=15551000&
    // SESSDATA=7f09bcbe%2C1607349092%2Cce900%2A61&
    // bili_jct=46fcfcaf79d3e8223353ef0848b15b96&
    // gourl=https%3A%2F%2Fpassport.bilibili.com%2Fajax%2FminiLogin%2Fredirect
}