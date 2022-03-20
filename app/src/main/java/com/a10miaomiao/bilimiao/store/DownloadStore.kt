package com.a10miaomiao.bilimiao.store

import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.download.BiliVideoEntry
import cn.a10miaomiao.download.DownloadInfo
import cn.a10miaomiao.miao.binding.MiaoBinding
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import org.kodein.di.DI
import java.lang.Exception

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