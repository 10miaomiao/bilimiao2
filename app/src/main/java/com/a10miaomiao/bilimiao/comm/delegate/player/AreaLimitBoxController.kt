package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
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
        areaLimitLayout.setOnClickListener(){
            return@setOnClickListener
        }
        areaLimitOkBtn.setOnClickListener {
            val nav = activity.findNavController(R.id.nav_bottom_sheet_fragment)
            nav.navigateToCompose(SelectProxyServerPage())
        }
        areaLimitCloseBtn.setOnClickListener {
            hide()
            delegate.closePlayer()
        }
    }

    fun show(source: BangumiPlayerSource) {
        areaLimitLayout.visibility = View.VISIBLE
        delegate.views.videoPlayer.visibility = View.GONE
        delegate.loadingBoxController.hideLoading()
    }
    fun hide() {
        areaLimitLayout.visibility = View.GONE
        delegate.views.videoPlayer.visibility = View.VISIBLE
    }

}