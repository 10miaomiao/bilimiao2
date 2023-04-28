package com.a10miaomiao.bilimiao.page

import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.gson.Gson
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.view
import splitties.views.topPadding
import java.util.*

class WebFragment : Fragment(), DIAware, MyPage {

    companion object {
        /**
         * User-Agent: Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36 os/android model/MuMu build/6710300 osVer/6.0.1 sdkInt/23 network/2 BiliApp/6710300 mobi_app/android channel/bili Buvid/XZB6797B5E07C2A4D64B31242957D7B1B9CDF sessionID/4bfd832a innerVer/6710300 c_locale/zh_CN s_locale/zh_CN disable_rcmd/0
         */
        val userAgent = """
            |os/android 
            |model/${Build.MODEL} 
            |build/6710300 
            |osVer/${Build.VERSION.RELEASE} 
            |sdkInt/${Build.VERSION.SDK_INT}  
            |network/2 
            |BiliApp/6710300 mobi_app/android 
            |channel/bili 
            |c_locale/zh_CN 
            |s_locale/zh_CN 
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")
    }

    private var pageTitle = "加载中"


    override val pageConfig = myPageConfig {
        title = pageTitle
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val ID_webView = View.generateViewId()
    private val ID_root = View.generateViewId()
    private var mWebView: WebView? = null
    private var mRootView: View? = null

    private val windowStore by instance<WindowStore>()

    private val userStore by instance<UserStore>()

    private var loading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    private fun updateLoading(value: Boolean) {
        ui.setState {
            loading = value
        }
    }

    private fun setPageTitle(title: String) {
        pageTitle = title
        pageConfig.notifyConfigChanged()
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            val re = BiliNavigation.navigationTo(view, url)
            if (re) {
                return true
            }
            if (url.indexOf("bilibili://") == 0) {
                toast("不支持打开的链接：$url")
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            updateLoading(true)
            if (url.indexOf("navhide=1") != -1) {
                mRootView?.topPadding = 0
            } else  {
                mRootView?.topPadding = windowStore.getContentInsets(view).top
            }
            view.evaluateJavascript("""
                (function(){
                    window.BiliJsBridge = {
                        sendTasks: [],
                        callbacks: [],
                        selfCallbackId: 0,
                        newVersion: true,
                        inited: true,
                    };
                    window.BiliJsBridge.biliInject = {
                        postMessage: function(e) {
                            window._BiliJsBridge.postMessage(e);
                        },
                        biliCallbackReceived: function(t, e, n) {
                            var r = window.BiliJsBridge.callbacks.map((function(t) {
                                return t.callbackId
                            })).indexOf(Number(t));
                            console.log(r)
                            r >= 0 && window.BiliJsBridge.callbacks[r].callback && window.BiliJsBridge.callbacks[r].callback(n || e)
                        }
                    }
                })()
            """.trimIndent()) {
//                DebugMiao.log("callback", it)
            }
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            updateLoading(false)
            val js = """javascript:(function() {
                        var parent = document.getElementsByTagName('head').item(0);
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = '#dynamic-openapp, #dynamic-openapp-mask,.mini-header-container,.fixed-header-container,.v-navbar__body,#internationalHeader,.international-footer,.bili-footer,#cannot-check{display: none !important;} #app{padding-bottom: 0;}';
                        parent.appendChild(style);
                        window.java_obj.showDescription(
                               'theme-color',
                               document.querySelector('meta[name="theme-color"]').getAttribute('content')
                        );
                        window.java_obj.test.hello('from js');
                    })()
                """
            view.loadUrl(js)
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            setPageTitle(title)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view.findViewById<View>(ID_root)
        if (mWebView == null) {
            val webView = view.findViewById<WebView>(ID_webView)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.webViewClient = mWebViewClient
            webView.webChromeClient = mWebChromeClient
            webView.settings.apply {
                javaScriptEnabled = true
                userAgentString = "$userAgentString $userAgent"

            }
            webView.addJavascriptInterface(BiliJsBridge(), "_BiliJsBridge")
//            webView.addJavascriptInterface(InJavaScriptLocalObj2(), "java_obj.test")
            val url = requireArguments().getString(MainNavGraph.args.url)
            if (url == null) {
                findNavController().popBackStack()
            } else {
                DebugMiao.log("webView", url)
                webView.loadUrl(url.replace("http://", "https://"))
            }
            mWebView = webView
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout(ID_root) {
            setBackgroundColor(config.blockBackgroundColor)
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
//            _topPadding = contentInsets.top
            _bottomPadding = windowStore.bottomAppBarHeight

            views {
                +view<WebView>(ID_webView) {
                    backgroundColor = config.windowBackgroundColor
                }..lParams(matchParent, matchParent)

                +progressBar {
                    _show = loading
                }..lParams {
                    width = dip(64)
                    height = dip(64)
                    gravity = Gravity.CENTER
                }
            }


        }
    }

    inner class BiliJsBridge {

        val allSupportMethod = listOf<String>(
            "global.closeBrowser",
            "ui.setStatusBarMode",
//            "auth.checkBridgeEnable",
            "auth.getUserInfo",
//            "auth.getAccessToken",
//            "auth.getBaseInfo",
//            "auth.getAllBridge",
//            "auth.getTeenable",
//            "auth.getNetEnv",
            "ability.openScheme",
            "ability.currentThemeType",
//            "view.goBack",
//            "view.closeBrowser",
//            "view.toast",
//            "view.refresh",
//            "view.setTitle",
//            "view.isLongScreen",
//            "route.login",
//            "route.editUserInfo",
//            "route.record",
//            "route.recommend",
//            "share.showShareWindow",
//            "share.setShareMpcContent",
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
        fun postMessage (eventString: String) {
            DebugMiao.log("postMessage", eventString)
            val event = Gson().fromJson(eventString, MessageEventInfo::class.java)
            var result = ""
            when (event.method) {
                "ui.setStatusBarMode" -> {

                }
                "auth.getUserInfo" -> {

                }
                "global.getAllSupport" -> {
                    result = "[${allSupportMethod.joinToString(",") { "\"$it\"" }}]"
                    DebugMiao.log(result)
                }
                "global.closeBrowser" -> {
                    findNavController().popBackStack()
                }
                "share.setShareContent" -> {
                    requireActivity().runOnUiThread {
                        toast("暂不支持分享操作")
                    }
                }
                "share.showShareMpcWindow" -> {
                    requireActivity().runOnUiThread {
                        toast("暂不支持分享操作")
                    }
                }
                "ability.openScheme" -> {
                    val url = event.data["url"] ?: return
                    DebugMiao.log(url)
                    requireActivity().runOnUiThread {
                        val re = BiliNavigation.navigationTo(
                            mWebView!!,
                            url
                        )
                        if (!re) {
                            toast("不支持打开的链接：$url")
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
            }
            requireActivity().runOnUiThread {
                event.callback(result)
            }
        }

        fun MessageEventInfo.callback(
            result: String
        ) {
            val callbackId = data["callbackId"]
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
            requireActivity().runOnUiThread {
                mWebView?.loadUrl("javascript:$javascript")
            }
//            DebugMiao.log("biliCallbackReceived", url)
//            mWebView?.loadUrl(url)
        }
    }

    data class MessageEventInfo(
        val method: String,
        val data: Map<String, String>
    )



}