package com.a10miaomiao.bilimiao.page.user

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import cn.a10miaomiao.miao.binding.android.view.*
import cn.a10miaomiao.miao.binding.miaoEffect
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.template.TemplateViewModel
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.*
import java.util.regex.Pattern

class UserFollowFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = if (userStore.isSelf(mid!!)) {
            "我的好友"
        } else {
            "${name}\n的\n好友"
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<TemplateViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val userStore by instance<UserStore>()

    private val type by lazy { requireArguments().getString(MainNavGraph.args.type) }
    private val mid by lazy { requireArguments().getString(MainNavGraph.args.id) }
    private val name by lazy { requireArguments().getString(MainNavGraph.args.name) }
    private var loading: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
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
                val args = bundleOf(
                    MainNavGraph.args.id to id,
                )
                Navigation.findNavController(view)
                    .navigate(MainNavGraph.action.userFollow_to_user, args)
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

    @OptIn(InternalSplittiesApi::class)
    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)
        frameLayout {
            setBackgroundColor(config.blockBackgroundColor)
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right
            _topPadding = contentInsets.top
            _bottomPadding = contentInsets.bottom

            views {
                miaoEffect(null, {
                    +view<WebView> {
                        backgroundColor = config.windowBackgroundColor
                        webViewClient = mWebViewClient
                        webChromeClient = mWebChromeClient
                        settings.apply {
                            javaScriptEnabled = true
                        }
                        var url = "https://space.bilibili.com/h5/follow?type=$type&mid=$mid"
//                        if (night) url += "&night=1"
                        loadUrl(url)
                    }..lParams(matchParent, matchParent)
                })

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