package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
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
    )

    val autoStopDurationFlow = MutableStateFlow(0)

    val autoStopDuration get() = autoStopDurationFlow.value

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private val playListStore: PlayListStore by di.instance()

    /** 当前播放项在播放列表中的下标（-1 表示不在列表中），连播逻辑与界面共用的唯一计算实现 */
    private fun listPositionOf(
        playerState: State,
        playListState: PlayListStore.State,
    ): Int {
        return if (playerState.aid.isBlank()) {
            -1
        } else {
            playListState.indexOfAid(playerState.aid)
        }
    }

    /**
     * 播放列表当前位置的响应式投影（冷流）。
     *
     * 不持有缓存快照，订阅时按当前状态即时计算，因此不存在「无人订阅时读到过期值」的问题。
     * 界面订阅需自带初值：`collectAsState(initial = getPlayListCurrentPosition())`；
     * 命令式逻辑（自动连播、播放列表页定位）请直接调用 [getPlayListCurrentPosition]。
     */
    val listPositionFlow: Flow<Int> =
        stateFlow.combine(playListStore.stateFlow) { playerState, playListState ->
            listPositionOf(playerState, playListState)
        }

    /**
     * 当前播放项在播放列表中的下标（-1 表示不在列表中）。
     *
     * 直接由当前状态计算，不受任何 Flow 订阅状态影响：从收藏夹等页面直接拉起播放器时
     * 无人订阅界面，后台/浮层播放结束后的自动连播依然能取到正确下标。
     */
    fun getPlayListCurrentPosition(): Int {
        return listPositionOf(state, playListStore.state)
    }

    fun setPlayerSource(source: BasePlayerSource) {
        val ids = source.getSourceIds()
        this.setState {
            cid = source.id
            title = source.title
            cover = source.coverUrl
            mainTitle = source.mainTitle
            sid = ids.sid
            epid = ids.epid
            aid = ids.aid
            type = if (ids.sid.isNotBlank()) {
                BANGUMI
            } else {
                VIDEO
            }
        }
        if (!playListStore.state.inListForAid(state.aid)) {
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
        }
    }

    fun nextVideo(
        isRandom: Boolean,
        isLoop: Boolean,
    ): PlayListItemInfo? {
        val playList = playListStore.state
        if (!playList.isEmpty()) {
            val currentPosition = getPlayListCurrentPosition()
            val listSize = playList.items.size
            if (isRandom && listSize > 1) {
                var randomPosition = (0 until listSize).random()
                while (randomPosition == currentPosition) {
                    randomPosition = (0 until listSize).random()
                }
                val nextVideo = playList.items[randomPosition]
                return nextVideo
            }
            if (currentPosition != -1
                && currentPosition < listSize - 1
            ) {
                val nextVideo = playList.items[currentPosition + 1]
                return nextVideo
            } else if (isLoop) {
                val nextVideo = playList.items[0]
                return nextVideo
            }
        }
        return null
    }

    fun setAutoStopDuration(duration: Int) {
        autoStopDurationFlow.value = duration
    }
}

