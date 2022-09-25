package com.a10miaomiao.bilimiao.store

import androidx.lifecycle.ViewModel
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadInfo
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI

class DownloadStore(override val di: DI) :
    ViewModel(), BaseStore<DownloadStore.State> {

    data class State (
        var curVideo: BiliVideoEntry? = null,
        var curDownload: DownloadInfo? = null,
        var progress: Long = 0L,
        var status: Int = 0,
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setVideoInfo(info: BiliVideoEntry?) {
        this.setState {
            this.curVideo = info
        }
    }


    fun setDownloadInfo(info: DownloadInfo) {
        this.setState {
            this.curDownload = info
            this.progress = info.progress
            this.status = info.status
        }
    }


    fun clearDownloadInfo() {
        this.setState {
            this.curDownload = null
            this.progress = 0L
            this.status = 0
        }
    }

}