package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import org.kodein.di.DI
import org.kodein.di.DIAware

class AreaLimitBoxController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware {

    val areaLimitLayout = activity.findViewById<RelativeLayout>(R.id.area_limit_layout)
    val areaLimitOkBtn = activity.findViewById<View>(R.id.area_limit_ok_btn)
    val areaLimitCloseBtn = activity.findViewById<View>(R.id.area_limit_close_btn)

    init {
        initAreaLimitBox()
    }

    /**
     * 区域限制
     */
    private fun initAreaLimitBox() {
        areaLimitOkBtn.setOnClickListener {
            val nav = activity.findNavController(com.a10miaomiao.bilimiao.R.id.nav_bottom_sheet_fragment)
            val url = "bilimiao://setting/proxy/select"
            nav.navigateToCompose(url)
        }
        areaLimitCloseBtn.setOnClickListener {
            hide()
            delegate.closePlayer()
        }
    }

    fun show(source: BangumiPlayerSource) {
        areaLimitLayout.visibility = View.VISIBLE
        delegate.views.videoPlayer.visibility = View.GONE
    }
    fun hide() {
        areaLimitLayout.visibility = View.GONE
        delegate.views.videoPlayer.visibility = View.VISIBLE
    }

}