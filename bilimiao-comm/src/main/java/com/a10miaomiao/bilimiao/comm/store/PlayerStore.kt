package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
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
            if (aid.isBlank()) {
                return 0
            }
            return playList?.run {
                items.indexOfFirst {
                    it.aid == aid
                }
            } ?: -1
        }

        fun inPlayList(aid: String): Boolean {
            playList?.run {
                items.forEach {
                    if(aid == it.aid){
                        return true
                    }
                }
            }
            return false
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
            from = info.id,
            items = items,
            type = 1,
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
            sid = ids.sid
            epid = ids.epid
            aid = ids.aid
            type = if (ids.sid.isNotBlank()) {
                BANGUMI
            } else {
                VIDEO
            }
            if (getPlayListCurrentPosition() == -1) {
                playList = null
            }
        }
    }

    suspend fun setFavoriteList(mediaId: String, mediaTitle: String) {
        val items = mutableListOf<PlayListItemInfo>()
        val pageSize = 20
        var pageNum = 1
        var loadFinish = false
        while(!loadFinish){
            try {
                val res = BiliApiService.userApi.mediaDetail(
                    media_id = mediaId,
                    keyword = "",
                    pageNum = pageNum,
                    pageSize = 20,
                ).awaitCall().gson<ResultInfo<MediaDetailInfo>>()
                if (res.code == 0) {
                    val result = res.data.medias
                    val newItems = result.map {
                        PlayListItemInfo(
                            aid = it.id,
                            cid = it.ugc.first_cid,
                            duration = it.duration.toInt(),
                            title = it.title,
                            cover = it.cover,
                            ownerId = it.upper.mid,
                            ownerName = it.upper.name,
                            from = mediaId,
                        )
                    }
                    items.addAll(newItems)
                    loadFinish = newItems.size != pageSize
                    pageNum++
                } else {
                    withContext(Dispatchers.Main) {
                        PopTip.show(res.message)
                    }
                    loadFinish = true
                }

            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    PopTip.show(e.toString())
                }
                loadFinish = true
            } finally {
            }
        }

        val aid = state.aid
        var currentVideoInList = false
        items.forEach{
            if(it.aid == aid){
                currentVideoInList = true
            }
        }
        if(aid.isNotEmpty() && !currentVideoInList) {
            //有视频正在播放 且当前视频不在列表中时，不设置列表
        } else {
            this.setState {
                playList = PlayListInfo(
                    name = mediaTitle,
                    from = mediaId,
                    items = items,
                    type = 2,
                )
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

