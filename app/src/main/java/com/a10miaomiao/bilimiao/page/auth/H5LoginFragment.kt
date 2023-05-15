package com.a10miaomiao.bilimiao.page.auth

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
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.store.WindowStore
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
import splitties.views.dsl.core.view

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
    }

    override val pageConfig = myPageConfig {
        title = "网页登录"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val ID_webView = 101

    private val viewModel by diViewModel<H5LoginViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private fun requestThird(view: WebView) {
        val nav = findNavController()
        nav.popBackStack(MainNavGraph.dest.main, true)
    }

    private val mWebViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//            return BilibiliRouter.gotoUrl(context!!, url)
            DebugMiao.log("shouldOverrideUrlLoading", url)
            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            DebugMiao.log("shouldOverrideUrlLoading2", url)
            return this.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            viewModel.updateLoading(true)
            DebugMiao.log("onPageStarted", url)
//            loading set true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            viewModel.updateLoading(false)
            DebugMiao.log("onPageFinished", url)
            if (url.contains("passport.bilibili.com/tv/passport/#/common/success")) {
                lifecycleScope.launch(Dispatchers.Main) {
                    viewModel.checkQRCode(isPolling = false)
                }
            } else if (url.contains("bilibili.com")) {
                val js = """javascript:(function() {
                        var parent = document.getElementsByTagName('head').item(0);
                        var style = document.createElement('style');
                        style.type = 'text/css';
                        style.innerHTML = '#internationalHeader,.international-footer,.bili-footer,#cannot-check,.open-app{display: none !important;}';
                        parent.appendChild(style);
                    })()
                """
                view.loadUrl(js)
            }
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            request?.url?.toString()?.let { url ->
                DebugMiao.log("onReceivedError", url)
//                if (url.contains("access_key=") && url.contains("mid=")) {
//                    viewModel.resolveUrl(view, url)
//                }
            }
        }

    }

    private val mWebChromeClient = object : WebChromeClient() {

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val webView = view.findViewById<WebView>(ID_webView)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webViewClient = mWebViewClient
        webView.webChromeClient = mWebChromeClient
        webView.settings.apply {
            javaScriptEnabled = true
        }

        var url = requireArguments().getString(MainNavArgs.url, "")
        if (url.isBlank()) {
            viewModel.getQrCodeUrl(webView)
        } else {
            webView.loadUrl(Uri.decode(url))
        }

        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        frameLayout {
            val contentInsets = windowStore.getContentInsets(parentView)
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom + windowStore.bottomAppBarHeight
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            views {
                +view<WebView>(ID_webView) {
                    backgroundColor = config.windowBackgroundColor
                }

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
