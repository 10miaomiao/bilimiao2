package com.a10miaomiao.bilimiao.widget.player

import android.view.MotionEvent

interface VideoPlayerCallBack {
    fun onPrepared()
    fun onAutoCompletion()
    fun onVideoPause()
    fun onVideoResume(isResume: Boolean)
    fun setStateAndUi(state: Int)
    fun onVideoClose()
    fun onClickUiToggle(e: MotionEvent?)
}