package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.delegate.player.BangumiPlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class PlayerStore(override val di: DI) :
    ViewModel(), BaseStore<PlayerStore.State> {

    companion object {
        const val VIDEO = "video"
        const val BANGUMI = "bangumi"
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
        var playList: PlayListInfo? = null,
        var playProgress: Long = 0,
    ) {
        fun getPlayListSize(): Int {
            return playList?.run { items.size } ?: 0
        }

        fun getPlayListCurrentPosition(): Int {
            if (cid.isBlank()) {
                return 0
            }
            return playList?.run {
                items.indexOfFirst {
                    it.cid == cid
                }
            } ?: -1
        }
    }

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setPlayProgress(progress:Long){
        this.setState {
            playProgress = progress
        }
    }
    fun setPlayList(info: PlayListInfo) {
        this.setState {
            playList = info
        }
    }

    fun setPlayList(info: UgcSeasonInfo, index: Int) {
        val items = info.sections[index].episodes.map {
            PlayListItemInfo(
                aid = it.aid,
                cid = it.cid,
                duration = it.duration,
                title = it.title,
                cover = it.cover,
                ownerId = it.author.mid,
                ownerName = it.author.name,
                from = info.id,
            )
        }
        val title = if (info.sections.size > 1) {
            info.title + "：" +info.sections[index].title
        } else {
            info.title
        }
        setPlayList(PlayListInfo(
            name = title,
            from = 1,
            items = items,
        ))
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
            if (ids.sid.isNotBlank() && ids.epid.isNotBlank()) {
                type = BANGUMI
                sid = ids.sid
                epid = ids.epid
            } else {
                type = VIDEO
                aid = ids.aid
            }
            if (getPlayListCurrentPosition() == -1) {
                playList = null
            }
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

