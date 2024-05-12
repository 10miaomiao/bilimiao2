package com.a10miaomiao.bilimiao.page.auth

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.tryPopBackStack
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.page.web.WebFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.web.NestedScrollWebView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
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

class H5LoginFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "auth.h5_login"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://auth/h5")
            deepLink("bilimiao://auth/h5/{url}")
            argument(MainNavArgs.url) {
                type = NavType.StringType
                defaultValue = ""
            }
        }
        fun createArguments(
            url: String
        ): Bundle {
            return bundleOf(
                MainNavArgs.url to url,
            )
        }

        private val ID_webView = View.generateViewId()
        private val ID_root = View.generateViewId()
    }

    override val pageConfig = myPageConfig {
        title = "网页登录"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private var mWebView: WebView? = null
    private var mRootView: View? = null

    private val viewModel by diViewModel<H5LoginViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private fun requestThird(view: WebView) {
        try {
            val nav = findNavController()
            nav.popBackStack(MainNavGraph.dest.main, true)
        } catch (e: Exception) {
        }
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            miaoLogger() debug (
                "url" to url
            )
            if (
                url.startsWith("https://www.bilibili.com/")
                || url.startsWith("https://m.bilibili.com/")
                || url.startsWith("https://bilibili.com/")
            ){
                viewModel.getQrCodeUrl(view)
                return true
            }
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
            viewModel.updateLoading(true)
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
            viewModel.updateLoading(false)
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
//            setPageTitle(title)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view.findViewById<View>(ID_root)
        if (mWebView == null) {
            val webView = view.findViewById<WebView>(ID_webView)
//            val biliJsBridge = BiliJsBridge(this, webView) {
//                viewModel.getQrCodeUrl(webView)
//            }
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.webViewClient = mWebViewClient
            webView.webChromeClient = mWebChromeClient
            webView.settings.apply {
                javaScriptEnabled = true
                var defaultUserAgentString = userAgentString
                if ("Mobile" !in defaultUserAgentString) {
                    defaultUserAgentString += " Mobile"
                }
                userAgentString = defaultUserAgentString
                allowContentAccess = true
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                databaseEnabled = true
                domStorageEnabled = true
                setSupportZoom(true)
                builtInZoomControls = true
                displayZoomControls = false
                loadsImagesAutomatically = true
            }
//            webView.addJavascriptInterface(biliJsBridge, "_BiliJsBridge")
            val url = requireArguments().getString(MainNavArgs.url, "")
            if (url.isBlank()) {
                webView.loadUrl("https://passport.bilibili.com/h5-app/passport/login")
            } else {
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
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom + windowStore.bottomAppBarHeight

            views {
                +view<WebView>(ID_webView) {
                    backgroundColor = config.windowBackgroundColor
                }..lParams(matchParent, matchParent)

                +progressBar {
                    _show = viewModel.loading
                }..lParams {
                    width = dip(64)
                    height = dip(64)
                    gravity = Gravity.CENTER
                }
            }


        }

    }
}
