package com.a10miaomiao.bilimiao.ui.setting

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
import android.preference.PreferenceFragment
import android.preference.PreferenceScreen
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.theme.ThemeFragment
import me.yokeyword.fragmentation.anim.DefaultHorizontalAnimator
import me.yokeyword.fragmentation.anim.DefaultVerticalAnimator
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout

class SettingFragment : SwipeBackFragment() {

    private val ID_PREFS_FRAME = 1

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = MainActivity.of(context!!).dynamicTheme(this) { createUI().view }
        return attachToSwipeBack(view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        MainActivity.of(context!!)
                .themeUtil
                .observeTheme(this, Observer {
                    initFragment()
                })
        initFragment()
    }

    private fun initFragment(){
        activity!!.fragmentManager.beginTransaction()
                .replace(ID_PREFS_FRAME, PreferenceFragment())
                .commit()
    }

    private fun createUI() = UI {
        verticalLayout {
            headerView {
                title("设置")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }
            frameLayout {
                id = ID_PREFS_FRAME
                backgroundColor = config.blockBackgroundColor
            }.lparams(matchParent, matchParent)
        }
    }

    class PreferenceFragment : android.preference.PreferenceFragment() {
        val about by lazy {
            findPreference("about") as Preference
        }
        val donate by lazy {
            findPreference("donate") as Preference
        }
        val help by lazy {
            findPreference("help") as Preference
        }
        val fragmentAnimator by lazy {
            findPreference("fragment_animator") as ListPreference
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference_setting)
            showVersion()
            fragmentAnimator.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { preference, newValue ->
                MainActivity.of(activity).fragmentAnimator = when (newValue as String) {
                    "vertical" -> DefaultVerticalAnimator()
                    "horizontal" -> DefaultHorizontalAnimator()
                    else -> DefaultVerticalAnimator()
                }
                true
            }
        }

        /**
         * 显示应用版本号
         */
        private fun showVersion() {
            val version = activity!!.packageManager.getPackageInfo(activity!!.packageName, 0).versionName
            about.summary = "版本：$version"
            help.summary = "世界太大，只能不停寻找"
        }


        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: android.preference.Preference?): Boolean {
            when (preference?.key) {
                "player" -> {
//                selectPalyer()
                    return true
                }
                "about" -> {
                    MainActivity.of(activity).start(AboutFragment())
                    return true
                }
                "video" -> {
                    MainActivity.of(activity).start(VideoSettingFragment())
                }
                "danmaku" -> {
                    MainActivity.of(activity).start(DanmakuSettingFragment())
                }
                "donate" -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    //HTTPS://QR.ALIPAY.COM/FKX07587MLQPOBBKACENE1
                    try {
                        intent.data = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                        startActivity(intent)
                    } catch (e: Exception) {
                        intent.data = Uri.parse("https://qr.alipay.com/FKX07587MLQPOBBKACENE1")
                        startActivity(intent)
                    }
                    return true
                }
                "theme" -> {
                    MainActivity.of(activity).start(ThemeFragment())
                }
                "help" -> {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://10miaomiao.cn/bilimiao/help.html")
                    startActivity(intent)
                }
            }
            return false
        }

    }

}