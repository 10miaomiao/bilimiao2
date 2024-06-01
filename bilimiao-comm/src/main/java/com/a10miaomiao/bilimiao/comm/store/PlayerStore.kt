package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import bilibili.app.dynamic.v2.ThreePointType
import bilibili.app.view.v1.ViewGRPC
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.instance


class PlayerStore(override val di: DI) :
    ViewModel(), BaseStore<PlayerStore.State> {

    companion object {
        const val VIDEO = "video"
        const val BANGUMI = "bangumi"

        const val FAVORITE = 2 // 收藏
        const val SEASON = 1 // 合集
    }

    data class State(
        var type: String = "",
        var aid: String = "",
        var cid: String = "",
        var epid: String = "",
        var sid: String = "",
        var mainTitle: String = "",
        var title: String = "",
        var cover: String = "",
        var playProgress: Long = 0,
    )

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val playListStore: PlayListStore by di.instance()

    fun getPlayListCurrentPosition(): Int {
        if (state.aid.isBlank()) {
            return -1
        }
        return playListStore.state
            .indexOfCid(state.cid)
    }

    fun setPlayProgress(progress:Long){
        this.setState {
            playProgress = progress
        }
    }

    fun setPlayerSource(source: BasePlayerSource) {
        val ids = source.getSourceIds()
        this.setState {
            cid = source.id
            title = source.title
            cover = source.coverUrl
            playProgress = 0
            if(source is VideoPlayerSource) {
                mainTitle = source.mainTitle
            } else {
                mainTitle = ""
            }
            sid = ids.sid
            epid = ids.epid
            aid = ids.aid
            type = if (ids.sid.isNotBlank()) {
                BANGUMI
            } else {
                VIDEO
            }
        }
        if (!playListStore.state.inListForCid(state.cid)) {
            // 当前视频，不在播放列表中，清除播放列表信息
            playListStore.clearPlayList()
        }
    }


    fun clearPlayerInfo() {
        this.setState {
            this.type = ""
            this.aid = ""
            this.cid = ""
            this.title = ""
            this.mainTitle = ""
            this.sid = ""
            this.epid = ""
            this.playProgress = 0
        }
    }
}

