package com.a10miaomiao.bilimiao.ui.setting

import android.annotation.SuppressLint
import android.os.Bundle
import android.preference.PreferenceScreen
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.frameLayout
import org.jetbrains.anko.matchParent
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout

class SettingFragment : SwipeBackFragment() {

    private val ID_PREFS_FRAME = 231434

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(createUI().view)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        var fragment = SettingPreferenceFragment()
        activity!!.fragmentManager.beginTransaction()
//        childFragmentManager.beginTransaction()
                .replace(ID_PREFS_FRAME, fragment)
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
            }.lparams(matchParent, matchParent)
        }
    }

}