package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.google.android.material.button.MaterialButton
import org.kodein.di.DI
import org.kodein.di.DIAware

class AreaLimitBoxController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware {

    val areaLimitLayout = activity.findViewById<RelativeLayout>(R.id.area_limit_layout)
    val areaLimitOkBtn = activity.findViewById<MaterialButton>(R.id.area_limit_ok_btn)
    val areaLimitCloseBtn = activity.findViewById<MaterialButton>(R.id.area_limit_close_btn)

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
            activity.openBottomSheet(SelectProxyServerPage())
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

    fun updateThemeColor(themeColor: Int) {
        areaLimitCloseBtn.setTextColor(themeColor)
        areaLimitCloseBtn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        areaLimitCloseBtn.rippleColor = ColorStateList.valueOf(0x66FFFFFF.toInt())
        areaLimitOkBtn.backgroundTintList = ColorStateList.valueOf(themeColor)
        areaLimitOkBtn.rippleColor = ColorStateList.valueOf(0x33000000)
    }

}