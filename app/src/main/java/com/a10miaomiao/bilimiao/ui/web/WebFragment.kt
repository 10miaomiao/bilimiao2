package com.a10miaomiao.bilimiao.ui.web

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.*
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import com.a10miaomiao.bilimiao.utils.BilibiliRouter
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.startFragment
import com.a10miaomiao.miaoandriod.MiaoLiveData
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.support.v4.UI

class WebFragment : SwipeBackFragment() {

    companion object {
        fun newInstance(url: String): WebFragment {
            val fragment = WebFragment()
            val bundle = Bundle()
            bundle.putString(ConstantUtil.URL, url)
            fragment.arguments = bundle
            return fragment
        }
    }

    private val url by lazy { arguments!!.getString(ConstantUtil.URL) }

    private val title = MiaoLiveData("")
    private val loading = MiaoLiveData(false)
    private var mWebView: WebView? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        night = MainActivity.of(context!!).themeUtil.getNight() == 2
        return attachToSwipeBack(createUI().view)
    }

    private val mWebViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            var url = request.url.toString()
            return BilibiliRouter.gotoUrl(context!!, url)
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            loading set true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            title set (view?.title ?: "")
            loading set false
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
                (+title)(::title)
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick { pop() }
                inflateMenu(R.menu.web)
                setOnMenuItemClickListener(Toolbar.OnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.share -> {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "text/plain"
                            intent.putExtra(Intent.EXTRA_TEXT, mWebView?.url ?: url)
                            startActivity(intent)
                        }
                        R.id.open -> {
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(mWebView?.url ?: url)
                            startActivity(intent)
                        }
                    }
                    true
                })
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
                    loadUrl(this@WebFragment.url)
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