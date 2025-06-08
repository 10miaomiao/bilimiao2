package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.a10miaomiao.bilimiao.compose.pages.setting.proxy.SelectProxyServerPage
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.navigation.openBottomSheet
import com.a10miaomiao.bilimiao.widget.player.VideoPlayerCallBack
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener
import org.kodein.di.DI
import org.kodein.di.DIAware

class ErrorMessageBoxController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware {

    val errorMessageLayout = activity.findViewById<RelativeLayout>(R.id.error_message_layout)
    val errorMessageText = activity.findViewById<TextView>(R.id.error_message_text)
    val errorMessageRetryBtn = activity.findViewById<View>(R.id.error_message_retry_btn)
    val errorMessageCloseBtn = activity.findViewById<View>(R.id.error_message_close_btn)

    init {
        initErrorMessageBox()
    }

    /**
     * 错误信息对话框
     */
    private fun initErrorMessageBox() {
        hide()
        errorMessageLayout.setOnClickListener(){
            return@setOnClickListener
        }
        errorMessageRetryBtn.setOnClickListener {
            if (delegate.playerSource?.proxyServer != null) {
                activity.openBottomSheet(SelectProxyServerPage())
            } else {
                hide()
                delegate.reloadPlayer()
            }
        }
        errorMessageCloseBtn.setOnClickListener {
            delegate.controller.smallScreen()
            hide()
            delegate.closePlayer()
        }
    }

    fun show(
        message: String,
        canRetry: Boolean = true
    ) {
        errorMessageText.text = message
        errorMessageLayout.visibility = View.VISIBLE
        errorMessageRetryBtn.isEnabled = canRetry
        delegate.views.videoPlayer.visibility = View.GONE
        delegate.loadingBoxController.hideLoading()
    }
    fun hide() {
        errorMessageText.text = ""
        errorMessageLayout.visibility = View.GONE
        delegate.views.videoPlayer.visibility = View.VISIBLE
    }

}