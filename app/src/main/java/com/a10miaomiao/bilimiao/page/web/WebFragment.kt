package com.a10miaomiao.bilimiao.page.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.miao.binding.android.view.*
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.tryPopBackStack
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.web.NestedScrollWebView
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

class WebFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        /**
         * User-Agent: Mozilla/5.0 (Linux; Android 6.0.1; MuMu Build/V417IR; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/61.0.3163.98 Mobile Safari/537.36 os/android model/MuMu build/6710300 osVer/6.0.1 sdkInt/23 network/2 BiliApp/6710300 mobi_app/android channel/bili Buvid/XZB6797B5E07C2A4D64B31242957D7B1B9CDF sessionID/4bfd832a innerVer/6710300 c_locale/zh_CN s_locale/zh_CN disable_rcmd/0
         */
        val userAgent = """
            |os/android 
            |model/${Build.MODEL} 
            |build/${ApiHelper.BUILD_VERSION} 
            |osVer/${Build.VERSION.RELEASE} 
            |sdkInt/${Build.VERSION.SDK_INT}  
            |network/2 
            |BiliApp/${ApiHelper.BUILD_VERSION} 
            |mobi_app/android_hd 
            |channel/bili 
            |c_locale/zh_CN 
            |s_locale/zh_CN 
            |disable_rcmd/0
        """.trimMargin().replace("\n", "")

        override val name = "web"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilibili://browser/?url={url}")
            argument(MainNavArgs.url) {
                type = NavType.StringType
                nullable = true
            }
        }

        fun createArguments(url: String): Bundle {
            return bundleOf(
                MainNavArgs.url to url
            )
        }

        private val ID_webView = View.generateViewId()
        private val ID_root = View.generateViewId()
    }

    private var pageTitle = "加载中"

    override val pageConfig = myPageConfig {
        title = pageTitle
        menus = listOf(
            myMenuItem {
                key = MenuKeys.more
                iconResource = R.drawable.ic_more_vert_grey_24dp
                title = "更多"
            },
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when(menuItem.key) {
            MenuKeys.more -> {
                val url = mWebView?.url ?: return
                WebMorePopupMenu(requireActivity(), view, url)
                    .show()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

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
            val re = BiliNavigation.navigationTo(NavHosts.pointerNavController, url)
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
//            val js = """javascript:(function() {
//                        var parent = document.getElementsByTagName('head').item(0);
//                        var style = document.createElement('style');
//                        style.type = 'text/css';
//                        style.innerHTML = '#dynamic-openapp, #dynamic-openapp-mask,.mini-header-container,.fixed-header-container,.v-navbar__body,#internationalHeader,.international-footer,.bili-footer,#cannot-check{display: none !important;} #app{padding-bottom: 0;}';
//                        parent.appendChild(style);
//                        window.java_obj.showDescription(
//                               'theme-color',
//                               document.querySelector('meta[name="theme-color"]').getAttribute('content')
//                        );
//                        window.java_obj.test.hello('from js');
//                    })()
//                """
//            view.loadUrl(js)
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {
        override fun onReceivedTitle(view: WebView, title: String) {
            super.onReceivedTitle(view, title)
            setPageTitle(title)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view.findViewById<View>(ID_root)
        if (mWebView == null) {
            val webView = view.findViewById<WebView>(ID_webView)
            val biliJsBridge = BiliJsBridge(this, webView)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.webViewClient = mWebViewClient
            webView.webChromeClient = mWebChromeClient
            webView.settings.apply {
                javaScriptEnabled = true
                var defaultUserAgentString = userAgentString
                if ("Mobile" !in defaultUserAgentString) {
                    defaultUserAgentString += " Mobile"
                }
                userAgentString = "$defaultUserAgentString $userAgent"
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
            webView.addJavascriptInterface(biliJsBridge, "_BiliJsBridge")
            val url = requireArguments().getString(MainNavArgs.url)
            if (url == null) {
                findNavController().tryPopBackStack()
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
            _bottomPadding = contentInsets.bottom
//            _bottomPadding = contentInsets.bottom + windowStore.bottomAppBarHeight

            views {
                +view<NestedScrollWebView>(ID_webView) {
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
}