package com.a10miaomiao.bilimiao.comm.delegate.player

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.a10miaomiao.bilimiao.R
import org.kodein.di.DI
import org.kodein.di.DIAware

class CompletionBoxController(
    private var activity: AppCompatActivity,
    private val delegate: PlayerDelegate2,
    override val di: DI,
) : DIAware {

    val completionLayout = activity.findViewById<RelativeLayout>(R.id.completion_layout)
    val completionText = activity.findViewById<TextView>(R.id.completion_text)
    val completionTextHold = activity.findViewById<TextView>(R.id.completion_text_hold)
    val completionRetryBtn = activity.findViewById<View>(R.id.completion_retry_btn)
    val completionCloseBtn = activity.findViewById<View>(R.id.completion_close_btn)

    init {
        initCompletionBox()
    }

    /**
     * 错误信息对话框
     */
    private fun initCompletionBox() {
        hide()
        completionLayout.setOnClickListener(){
            return@setOnClickListener
        }
        completionRetryBtn.setOnClickListener {
            hide()
            delegate.reloadPlayer()
        }
        completionCloseBtn.setOnClickListener {
            delegate.controller.smallScreen()
            hide()
            delegate.closePlayer()
        }
    }

    fun show() {
        completionLayout.visibility = View.VISIBLE
        delegate.views.videoPlayer.visibility = View.GONE
    }
    fun hide() {
        completionLayout.visibility = View.GONE
        delegate.views.videoPlayer.visibility = View.VISIBLE
    }

    fun setHoldStatus(isHold:Boolean){
        if(isHold){
            completionCloseBtn.visibility = View.GONE
            completionRetryBtn.visibility = View.GONE
            completionText.visibility = View.GONE
            completionTextHold.visibility = View.VISIBLE
        } else {
            completionCloseBtn.visibility = View.VISIBLE
            completionRetryBtn.visibility = View.VISIBLE
            completionText.visibility = View.VISIBLE
            completionTextHold.visibility = View.GONE
        }
    }
}