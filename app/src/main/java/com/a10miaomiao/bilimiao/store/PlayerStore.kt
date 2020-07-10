package com.a10miaomiao.bilimiao.store

import android.arch.lifecycle.ViewModel
import android.content.Context
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.miaoandriod.MiaoLiveData

class PlayerStore(
        val context: Context
) : ViewModel() {

    private val _info = MiaoLiveData(PlayerInfo())

    val info get() = -_info

    fun observe() = _info.observe()

    fun setBangumiPlayerInfo(sid: String, epid: String, cid: String, title: String) {
        _info set PlayerInfo(
                type = ConstantUtil.BANGUMI,
                sid = sid,
                epid = epid,
                cid = cid,
                title = title
        )
    }

    fun setVideoPlayerInfo(aid: String, cid: String, title: String) {
        _info set PlayerInfo(
                type = ConstantUtil.VIDEO,
                aid = aid,
                cid = cid,
                title = title
        )
    }

    fun clearPlayerInfo() {
        _info set PlayerInfo()
    }

    data class PlayerInfo(
            var type: String = "",
            var aid: String = "",
            var cid: String = "",
            var epid: String = "",
            var sid: String = "",
            var title: String = ""
    )
}