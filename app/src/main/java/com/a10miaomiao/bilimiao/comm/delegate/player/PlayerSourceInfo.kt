package com.a10miaomiao.bilimiao.comm.delegate.player

import cn.a10miaomiao.download.BiliVideoEntry

class PlayerSourceInfo {
    var type = ""
    var aid = ""
    var cid = ""
    var epid = ""
    var sid = ""
    var title = ""
    var localEntry: BiliVideoEntry? = null

    fun setVideo(aid: String, cid: String, title: String) {
        this.type = VIDEO
        this.aid = aid
        this.cid = cid
        this.title = title
    }

    fun setBangumi(sid: String, epid: String, cid: String, title: String) {
        this.type = BANGUMI
        this.sid = sid
        this.epid = epid
        this.cid = cid
        this.title = title
    }

    fun setLocalVideo(biliVideo: BiliVideoEntry) {
        this.localEntry = biliVideo
        this.type = LOCAL_VIDEO
        this.aid = biliVideo.avid.toString()
        this.cid = biliVideo.page_data.cid.toString()
        this.title = biliVideo.title
    }

    fun reset() {
        this.type = ""
        this.sid = ""
        this.epid = ""
        this.cid = ""
        this.title = ""
        this.sid = ""
        this.epid = ""
    }

    companion object {
        const val VIDEO = "video"
        const val BANGUMI = "bangumi"
        const val LOCAL_VIDEO = "local_video"
    }
}