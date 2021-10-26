package com.a10miaomiao.bilimiao.ui.setting

import android.arch.lifecycle.Observer
import android.content.*
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceManager
import android.preference.PreferenceScreen
import android.preference.SwitchPreference
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.utils.DebugMiao
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout

class DanmakuSettingFragment : SwipeBackFragment() {

    companion object {
        const val UPDATE_ACTION = "com.a10miaomiao.bilimiao.ui.setting.DanmakuSettingFragment.UPDATE"
    }

    private val ID_PREFS_FRAME = 3

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
                title("弹幕设置")
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

    class PreferenceFragment : android.preference.PreferenceFragment(),
        SharedPreferences.OnSharedPreferenceChangeListener {

        val danmakuShow by lazy {
            findPreference("danmaku_show") as SwitchPreference
        }

        private val mBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                update(context)
            }
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference_danmaku_setting)
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
            val intentFilter = IntentFilter()
            intentFilter.addAction(UPDATE_ACTION)
            activity.registerReceiver(mBroadcastReceiver, intentFilter)
        }

        override fun onDestroy() {
            super.onDestroy()
            activity.unregisterReceiver(mBroadcastReceiver)
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
        }

        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: android.preference.Preference?): Boolean {
            return false
        }

        fun update(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            danmakuShow.isChecked = prefs.getBoolean("danmaku_show", true)
        }

        override fun onSharedPreferenceChanged(sp: SharedPreferences?, key: String) {
            if ("danmaku" in key) {
                MainActivity.of(activity).videoPlayerDelegate.updateDanmukuSetting()
            }
        }

    }



}