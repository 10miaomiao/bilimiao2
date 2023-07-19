package com.a10miaomiao.bilimiao.comm.delegate.player


class PlayerParamInfo {
    var type = ""
    var aid = ""
    var cid = ""
    var epid = ""
    var sid = ""
    var title = ""

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

    fun reset() {
        this.type = ""
        this.aid = ""
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