package com.a10miaomiao.bilimiao.ui.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.verticalLayout

class SettingFragment : SwipeBackFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return attachToSwipeBack(createUI().view)
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
        }
    }

}