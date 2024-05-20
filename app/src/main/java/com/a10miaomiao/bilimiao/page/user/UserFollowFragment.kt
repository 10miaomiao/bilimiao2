package com.a10miaomiao.bilimiao.page.user

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._show
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.comm.connectStore
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.currentOrSelf
import com.a10miaomiao.bilimiao.comm.progressBar
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.web.NestedScrollWebView
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.view
import java.util.regex.Pattern

class UserFollowFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.follow"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://user/follow?id={id}&type={type}&name={name}")
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.type) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.name) {
                type = NavType.StringType
                defaultValue = "Ta"
            }
        }
        fun createArguments(
            id: String,
            type: String,
            name: String = "Ta",
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.type to type,
                MainNavArgs.name to name,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = if (userStore.isSelf(mid!!)) {
            "我的好友"
        } else {
            "${name}\n的\n好友"
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val ID_webView = View.generateViewId()
    private var mWebView: WebView? = null

    private val windowStore by instance<WindowStore>()

    private val userStore by instance<UserStore>()

    private val themeDelegate by instance<ThemeDelegate>()

    private val type by lazy { requireArguments().getString(MainNavArgs.type) }
    private val mid by lazy { requireArguments().getString(MainNavArgs.id) }
    private val name by lazy { requireArguments().getString(MainNavArgs.name) }
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

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            val compile = Pattern.compile(".*://space.bilibili.com/(\\d+)")
            val matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                val args = UserFragment.createArguments(id)
                Navigation.findNavController(view).currentOrSelf()
                    .navigate(UserFragment.actionId, args)
                return true
            }
            if (isNightTheme()){
                url += if("?" in url){
                    "&night=1"
                }else{
                    "?night=1"
                }
                view.loadUrl(url)
                return true
            }
            return false
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            updateLoading(true)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            updateLoading(false)
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (mWebView == null) {
            val webView = view.findViewById<WebView>(ID_webView)
            CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            webView.webViewClient = mWebViewClient
            webView.webChromeClient = mWebChromeClient
            webView.settings.apply {
                javaScriptEnabled = true
            }
            var url = "https://space.bilibili.com/h5/follow?type=$type&mid=$mid"
            if (isNightTheme()) url += "&night=1"
            webView.loadUrl(url)
            mWebView = webView
        }
    }

    private fun isNightTheme() = !requireContext().config.isLightTheme

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout {
            setBackgroundColor(config.blockBackgroundColor)
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom

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