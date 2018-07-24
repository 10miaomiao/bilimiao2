package com.a10miaomiao.bilimiao.ui

import android.os.Build
import android.os.Bundle
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
import com.a10miaomiao.bilimiao.utils.RxBus
import me.yokeyword.fragmentation.SupportActivity
import java.nio.file.Files.size
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.FragmentAnimator




class MainActivity : SupportActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (findFragment(MainFragment::class.java) == null) {
            loadRootFragment(R.id.rootContainer, MainFragment())
        }
        RxBus.getInstance().on(ConstantUtil.START_FRAGMENT){
            if (it is BaseFragment) start(it)
        }

    }

    override fun onBackPressedSupport() {
        // 对于 4个类别的主Fragment内的回退back逻辑,已经在其onBackPressedSupport里各自处理了
        super.onBackPressedSupport()
    }

    override fun onCreateFragmentAnimator(): FragmentAnimator {
        // 设置横向(和安卓4.x动画相同)
        return DefaultHorizontalAnimator()
    }
}