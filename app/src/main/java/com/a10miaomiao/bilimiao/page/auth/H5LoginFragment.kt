package com.a10miaomiao.bilimiao.page.auth

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import cn.a10miaomiao.miao.binding.android.view._show
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp
import com.a10miaomiao.bilimiao.config.config
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

    override val pageConfig = myPageConfig {
        title = "网页登录"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val ID_webView = 101

    private val viewModel by diViewModel<H5LoginViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private fun requestThird(view: WebView) = lifecycle.coroutineScope.launch(Dispatchers.IO) {
        try {
            val res = MiaoHttp.request {
                url = "https://passport.bilibili.com/login/app/third?appkey=27eb53fc9058f8c3&api=http%3A%2F%2Flink.acg.tv%2Fforum.php&sign=67ec798004373253d60114caaad89a8c"
            }.call().body()!!.string()
            val json = JSONObject(res)
            withContext(Dispatchers.Main) {
                if (json.getInt("code") == 0) {
                    // https://passport.bilibili.com/login/appSuccess?api=http%3A%2F%2Flink.acg.tv%2Fforum.php&appkey=27eb53fc9058f8c3&sign=67ec798004373253d60114caaad89a8c&mhash=1ef1f2f0a48c2d0bb35951d9b7948e17&confirm=1
                    view.loadUrl(json.getJSONObject("data").getString("confirm_uri"))
                } else {
                    toast("登录失败，请重试");
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                toast("登录过程发生错误")
            }
        }
    }

    private val mWebViewClient = object : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
//            return BilibiliRouter.gotoUrl(context!!, url)
            return false
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            return this.shouldOverrideUrlLoading(view, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            viewModel.updateLoading(true)
//            loading set true
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            viewModel.updateLoading(false)
            if (url.contains("access_key=") && url.contains("mid=")) {
                viewModel.resolveUrl(view, url)
            } else if (url == "https://passport.bilibili.com/ajax/miniLogin/redirect") {
                requestThird(view)
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

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            request?.url?.toString()?.let { url ->
                if (url.contains("access_key=") && url.contains("mid=")) {
                    viewModel.resolveUrl(view, url)
                }
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
        webView.loadUrl("https://passport.bilibili.com/ajax/miniLogin/minilogin")
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        frameLayout {

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
