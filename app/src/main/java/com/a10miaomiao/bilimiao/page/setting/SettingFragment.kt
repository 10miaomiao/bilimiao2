package com.a10miaomiao.bilimiao.template

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.comm.entity.miao.MiaoSettingInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.utils.GlideCacheUtil
import com.a10miaomiao.bilimiao.page.filter.FilterListFragment
import com.a10miaomiao.bilimiao.page.setting.*
import com.a10miaomiao.bilimiao.store.RegionStore
import com.a10miaomiao.bilimiao.store.WindowStore
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*
import de.Maxr1998.modernpreferences.preferences.SwitchPreference
import de.Maxr1998.modernpreferences.preferences.choice.SelectionItem
import org.kodein.di.*
import splitties.toast.toast
import splitties.views.dsl.core.*
import splitties.views.dsl.recyclerview.recyclerView
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class SettingFragment : Fragment(), DIAware, MyPage
    , SharedPreferences.OnSharedPreferenceChangeListener {

    companion object : FragmentNavigatorBuilder() {
        override val name = "setting"
        override fun FragmentNavigatorDestinationBuilder.init() {
            deepLink("bilimiao://setting")
        }
    }

    override val pageConfig = myPageConfig {
        title = "设置"
    }

    override val di: DI by lazyUiDi(ui = { ui })

//    private val viewModel by diViewModel<TemplateViewModel>(di)

    private val windowStore by instance<WindowStore>()
    private val regionStore by instance<RegionStore>()

    private var mPreferencesAdapter: PreferencesAdapter? = null

    private val themeDelegate by instance<ThemeDelegate>()

    private var themeId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (themeDelegate.getThemeResId() != themeId) {
            ui.cleanCacheView()
            themeId = themeDelegate.getThemeResId()
        }
        ui.parentView = container
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    private fun createPreferenceClick(setting: MiaoSettingInfo): Preference.OnClickListener {
        return Preference.OnClickListener { _, _ ->
            val intent = Intent(Intent.ACTION_VIEW)
            try {
                intent.data = Uri.parse(setting.url)
                startActivity(intent)
            } catch (e: Exception) {
                if (setting.backupUrl != null) {
                    intent.data = Uri.parse(setting.backupUrl)
                    startActivity(intent)
                }
            }
            true
        }
    }

    private fun readSettingList(): List<MiaoSettingInfo> {
        try {
            val file = File(requireContext().filesDir, "settingList.json")
            if (!file.exists()) {
                return listOf()
            }
            val inputStream = requireContext().openFileInput("settingList.json")
            val br = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var str: String? = br.readLine()
            while (str != null) {
                stringBuilder.append(str)
                str = br.readLine()
            }
            val jsonStr = stringBuilder.toString()
            return Gson().fromJson(
                jsonStr,
                object : TypeToken<List<MiaoSettingInfo>>() {}.type,
            )
        } catch (e: IOException) {
            e.printStackTrace()
            return listOf()
        }
    }

    private fun showGlideImageCache() {
        MaterialAlertDialogBuilder(requireContext()).apply {
            val cacheSize = GlideCacheUtil.getCacheSize(requireContext())
            setTitle("提示")
            setMessage("确定清空图片缓存？当前缓存大小：$cacheSize")
            setNegativeButton("取消", null)
            setPositiveButton("确定") { text, d ->
                GlideCacheUtil.clearImageAllCache(requireContext())
                toast("清理完成，已清理$cacheSize")
                mPreferencesAdapter?.currentScreen?.let { screen ->
                    val danmakuShowSP = screen["glide_image_cache"] as Preference
                    danmakuShowSP.summary = "刚刚清理"
                }
                mPreferencesAdapter?.notifyDataSetChanged()
            }
        }.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            "is_best_region" -> {
                regionStore.loadRegionData(requireContext())
            }
        }
    }

    val ui = miaoBindingUi {
        connectStore(viewLifecycleOwner, windowStore)
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
                    miaoEffect(null) {
                        adapter = mAdapter
                    }
                    mPreferencesAdapter = mAdapter
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
            summary = "奇迹和魔法并不存在"
            defaultValue = false
            enabled = false
        }

        switch("is_best_region") {
            title = "使用旧版分区"
            summary = "你知道雪为什么是白色的吗"
            defaultValue = false
        }

        singleChoice("fragment_animator", listOf(SelectionItem("key", "title"))) {
            title = "请选择动画效果"
            summary = "每段四季，每轮星移，时光长旅，漫漫行迹..."
            enabled = false
        }

        pref("theme") {
            title = "切换主题"
            summary = "库洛里多创造的库洛牌啊，请你舍弃旧形象，以小樱之名命令你，封印解除！！！"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(ThemeSettingFragment.actionId)
                true
            }
        }

        pref("home") {
            title = "首页设置"
            summary = "整个宇宙将为你闪烁"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(HomeSettingFragment.actionId)
                true
            }
        }

        pref("video") {
            title = "播放设置"
            summary = "咖啡拿铁,咖啡摩卡,卡布奇诺!"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(VideoSettingFragment.actionId)
                true
            }
        }

        pref("danmaku") {
            title = "弹幕设置"
            summary = "相信的心就是你的魔法"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(DanmakuSettingFragment.actionId)
                true
            }
        }

        pref("filter") {
            title = "屏蔽管理"
            summary = "只对时光机生效"
            onClick {
                val nav = requireActivity().findNavController(R.id.nav_host_fragment)
                nav.navigate(FilterListFragment.actionId)
                true
            }
        }

        switch("auto_check_update") {
            title = "自动检测新版本"
            summary = "已经没有什么好害怕的了"
            defaultValue = true
        }

        pref("glide_image_cache") {
            title = "图片缓存"
            val cacheSize = GlideCacheUtil.getCacheSize(requireContext())
            summary = cacheSize
            onClick {
                showGlideImageCache()
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
                nav.navigate(AboutFragment.actionId)
                true
            }
        }

        readSettingList().forEach {
            if (it.type == "pref") {
                pref(it.name) {
                    title = it.title
                    summary = it.summary
                    clickListener = createPreferenceClick(it)
                }
            }
        }

    }


}