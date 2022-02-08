package com.a10miaomiao.bilimiao.template

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.android.widget._text
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.recycler.MiaoBindingAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.store.WindowStore
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*
import kotlinx.coroutines.launch
import org.kodein.di.*
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView

class SettingFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

//    private val viewModel by diViewModel<TemplateViewModel>(di)

    private val windowStore by instance<WindowStore>()

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

    val ui = miaoBindingUi {
        val insets = windowStore.getContentInsets(parentView)
        frameLayout {
            _leftPadding = insets.left
            _topPadding = insets.top
            _rightPadding = insets.right
            _bottomPadding = insets.bottom
            views {
                +recyclerView {
                    _miaoLayoutManage(LinearLayoutManager(requireContext()))
                    val mAdapter = miaoMemo(null) {
                        PreferencesAdapter(createRootScreen())
                    }
                    miaoEffect(null, {
                        adapter = mAdapter
                    })
//                    _miaoAdapter()
//
//                    preferencesView = binding.recyclerView.apply {
//                        layoutManager = this@BaseActivity.layoutManager
//                        adapter = preferencesAdapter
//                        layoutAnimation = AnimationUtils.loadLayoutAnimation(this@BaseActivity, R.anim.preference_layout_fall_down)
//                    }
                }..lParams(matchParent, matchParent)
            }
        }
    }

    fun createRootScreen() = screen(context) {
        collapseIcon = true

        categoryHeader("currency") {
            title = "常规"
        }

        switch("is_bili_player") {
            title = "使用外部播放器"
            summary = "奇迹和魔法都是存在的"
            defaultValue = false
        }

        switch("is_best_region") {
            title = "使用旧版分区"
            summary = "你知道雪为什么是白色的吗"
            defaultValue = false
        }

//        singleChoice("fragment_animator", listOf()) {
//            title = "请选择动画效果"
//            summary = "每段四季，每轮星移，时光长旅，漫漫行迹..."
//        }

        pref("theme") {
            title = "切换主题"
            summary = "库洛里多创造的库洛牌啊，请你舍弃旧形象，以小樱之名命令你，封印解除！！！"
        }

        pref("video") {
            title = "播放设置"
            summary = "咖啡拿铁,咖啡摩卡,卡布奇诺!"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(MainNavGraph.action.setting_to_videoSetting)
                true
            }
        }

        pref("danmaku") {
            title = "弹幕设置"
            summary = "相信的心就是你的魔法"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(MainNavGraph.action.setting_to_danmakuSetting)
                true
            }
        }

        categoryHeader("other") {
            title = "其它"
        }

        pref("about") {
            val version = requireActivity().run {
                packageManager.getPackageInfo(packageName, 0).versionName
            }
            title = "关于"
            summary = "版本：$version"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(MainNavGraph.action.setting_to_about)
                true
            }
        }

        pref("donate") {
            title = "捐助"
            summary = "我在这里哦。"
            onClick {
                val intent = Intent(Intent.ACTION_VIEW)
                //HTTPS://QR.ALIPAY.COM/FKX07587MLQPOBBKACENE1
                try {
                    intent.data = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                    startActivity(intent)
                } catch (e: Exception) {
                    intent.data = Uri.parse("https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                    startActivity(intent)
                }
                true
            }

        }

        pref("help") {
            title = "帮助"
            summary = "世界太大，只能不停寻找"
            onClick {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("https://10miaomiao.cn/bilimiao/help.html")
                startActivity(intent)
                true
            }
        }


    }


}