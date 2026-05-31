package com.a10miaomiao.bilimiao.comm.delegate.player

import android.content.res.Configuration

interface PlayerHostState {
    var showPlayer: Boolean
    var fullScreenPlayer: Boolean
    var orientation: Int
    var smallModePlayerMaxHeight: Int

    fun animatePlayerHeight(target: Int)
    fun holdUpPlayer()

    companion object {
        const val VERTICAL = Configuration.ORIENTATION_PORTRAIT
        const val HORIZONTAL = Configuration.ORIENTATION_LANDSCAPE
    }
}
