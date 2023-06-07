package com.a10miaomiao.bilimiao.page.user

import android.graphics.Bitmap
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import cn.a10miaomiao.miao.binding.android.view.*
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import java.util.regex.Pattern

class UserFollowFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.follow"
        override fun FragmentNavigatorDestinationBuilder.init() {
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
                Navigation.findNavController(view)
                    .navigate(UserFragment.actionId, args)
                return true
            }
//            if (night){
//                url += if("?" in url){
//                    "&night=1"
//                }else{
//                    "?night=1"
//                }
//                view.loadUrl(url)
//                return true
//            }
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
            var url = ""
//      if (night) url += "&night=1"
            webView.loadUrl("https://space.bilibili.com/h5/follow?type=$type&mid=$mid")
            mWebView = webView
        }
    }

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout {
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