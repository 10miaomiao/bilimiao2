package com.a10miaomiao.bilimiao.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.view.View
import android.view.WindowManager
import com.a10miaomiao.miaoandriod.MiaoActivity
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.home.HomeFragment
import com.a10miaomiao.bilimiao.ui.home.RankFragment
import kotlinx.android.synthetic.main.activity_main.*
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.widget.DrawerLayout
import com.a10miaomiao.bilimiao.base.BaseFragment
import com.a10miaomiao.bilimiao.ui.home.MainFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.RxBus
import me.yokeyword.fragmentation.SupportActivity
import me.yokeyword.fragmentation.SupportFragment
import java.nio.file.Files.size
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator
import android.view.WindowInsets
import com.a10miaomiao.bilimiao.ui.bangumi.BangumiFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.utils.getStatusBarHeight
import me.yokeyword.fragmentation.ISupportFragment
import me.yokeyword.fragmentation.SupportHelper


class MainActivity : SupportActivity() {

    var windowInsets: WindowInsets? = null
    val behavior by lazy {
        BottomSheetBehavior.from(bottomSheet)
    }
    var bottomSheetFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            windowInsets = rootLayout.rootWindowInsets
        }
        initBottomSheet()
        if (findFragment(MainFragment::class.java) == null) {
//            loadRootFragment(R.id.rootContainer, BangumiFragment.newInstance("24951"))
            loadRootFragment(R.id.rootContainer, MainFragment())
        }
//        loadRootFragment(R.id.rightContainer, SearchFragment())
    }

    override fun start(toFragment: ISupportFragment?) {
//        loadMultipleRootFragment(R.id.rightContainer,1, toFragment)
//        if (SupportHelper.getActiveFragment(supportFragmentManager) == null) {
//            loadMultipleRootFragment()
//        } else {
//            super.start(toFragment)
//        }

        if (toFragment is VideoInfoFragment) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(this)
            // 判断是否选择了使用 外部播放器
            if (prefs.getBoolean("is_bili_player", false)) {
                val id = toFragment.arguments!!.getString(ConstantUtil.ID)
                try {
                    var intent = Intent(Intent.ACTION_VIEW)
                    var url = "bilibili://video/$id"
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                } catch (e: Exception) {
                    var intent = Intent(Intent.ACTION_VIEW)
                    var url = "http://www.bilibili.com/video/av$id"
                    intent.data = Uri.parse(url)
                    startActivity(intent)
                }
                return
            }
        }
        super.start(toFragment)
    }

    override fun onBackPressedSupport() {
        if (behavior.state == BottomSheetBehavior.STATE_HIDDEN)
            super.onBackPressedSupport()
        else
            hideBottomSheet()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator {
        // 设置横向(和安卓4.x动画相同)
        return DefaultHorizontalAnimator()
    }

    private fun initBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
                if (p1 < 0) {
                    shadeView.alpha = (p1 + 1) * 0.6f
                } else {
                    shadeView.alpha = 0.6f
                }

            }

            override fun onStateChanged(p0: View, p1: Int) {
                if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                    shadeView.visibility = View.GONE
                    bottomSheetFragment?.let {
                        supportFragmentManager.beginTransaction()
                                .remove(it)
                                .commit()
                        bottomSheetFragment = null
                    }
                } else {
                    shadeView.visibility = View.VISIBLE
                }
            }
        })

        shadeView.setOnClickListener { hideBottomSheet() }
        (bottomSheet.layoutParams as CoordinatorLayout.LayoutParams)
                .setMargins(0, getStatusBarHeight(), 0, 0)
        shadeView.visibility =
                if (behavior.state == BottomSheetBehavior.STATE_HIDDEN)
                    View.GONE
                else
                    View.VISIBLE
    }

    fun showBottomSheet(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.bottomSheettContainer, fragment)
                .commit()
        bottomSheetFragment = fragment
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hideBottomSheet() {
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    companion object {
        fun of(context: Context): MainActivity {
            if (context is MainActivity) {
                return context
            } else {
                throw Exception()
            }
        }
    }


}