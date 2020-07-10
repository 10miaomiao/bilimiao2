package com.a10miaomiao.bilimiao.ui.user

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.entity.Owner
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.LoadMoreView
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.upper.UpperInfoFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.startFragment
import com.a10miaomiao.miaoandriod.MiaoLiveData
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.swipeRefreshLayout
import java.util.regex.Pattern

class UserFollowFragment : SwipeBackFragment() {

    companion object {
        const val TYPE = "type"
        const val TYPE_FANS = "fans"
        const val TYPE_FOLLOW = "follow"

        fun newInstance(type: String, mid: Long): UserFollowFragment {
            val fragment = UserFollowFragment()
            val bundle = Bundle()
            bundle.putString(TYPE, type)
            bundle.putLong(ConstantUtil.ID, mid)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val type by lazy { arguments!!.getString(TYPE) }
    private val mid by lazy { arguments!!.getLong(ConstantUtil.ID) }
    private val loading = MiaoLiveData(false)
    private var night = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        night = MainActivity.of(context!!).themeUtil.getNight() == 2
        return attachToSwipeBack(createUI().view)
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            val compile = Pattern.compile(".*://space.bilibili.com/(\\d+)")
            val matcher = compile.matcher(url)
            if (matcher.find()) {
                val id = matcher.group(1)
                startFragment(UserFragment.newInstance(id.toLong()))
                return true
            }
            if (night){
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
            loading set true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            loading set false
        }
    }

    private val mWebChromeClient = object : WebChromeClient() {

    }

    private fun createUI() = UI {
        val userStore = Store.from(context!!).userStore
        verticalLayout {
            headerView {
                if (userStore.isSelf(mid)) {
                    title("我的好友")
                } else {
                    title("Ta的好友")
                }
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
            }

            frameLayout {
                webView {
                    backgroundColor = config.windowBackgroundColor
                    webViewClient = mWebViewClient
                    webChromeClient = mWebChromeClient
                    settings.apply {
                        javaScriptEnabled = true
                    }
                    var url = "https://space.bilibili.com/h5/follow?type=$type&mid=$mid"
                    if (night) url += "&night=1"
                    loadUrl(url)
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

}