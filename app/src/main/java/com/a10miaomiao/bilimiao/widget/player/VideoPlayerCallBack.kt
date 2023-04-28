package com.a10miaomiao.bilimiao.widget.player

interface VideoPlayerCallBack {
    fun onPrepared()
    fun onVideoPause()
    fun onVideoResume(isResume: Boolean)
    fun setStateAndUi(state: Int)
    fun onVideoClose()
}