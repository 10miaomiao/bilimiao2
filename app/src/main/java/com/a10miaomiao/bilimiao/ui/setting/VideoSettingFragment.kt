package com.a10miaomiao.bilimiao.ui.setting

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.ListPreference
import android.preference.Preference
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

class VideoSettingFragment : SwipeBackFragment() {

    private val ID_PREFS_FRAME = 234324

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
                title("播放设置")
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


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.preference_video_setting)
        }


        override fun onPreferenceTreeClick(preferenceScreen: PreferenceScreen?, preference: android.preference.Preference?): Boolean {

            return false
        }

    }

}