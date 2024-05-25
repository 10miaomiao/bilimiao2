package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import bilibili.app.dynamic.v2.ThreePointType
import bilibili.app.view.v1.ViewGRPC
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
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
        var playList: PlayListInfo? = null,
        var playProgress: Long = 0,

        var playListLoading: Boolean = false,
    ) {
        fun getPlayListSize(): Int {
            return playList?.run { items.size } ?: 0
        }

        fun getPlayListCurrentPosition(): Int {
            if (aid.isBlank()) {
                return -1
            }
            return playList?.run {
                items.indexOfFirst {
                    it.cid == cid
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
    fun setPlayList(info: PlayListInfo?) {
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
            type = SEASON,
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

    val playListLoadingMutex = Mutex()
    suspend fun setFavoriteList(mediaId: String, mediaTitle: String)
    = playListLoadingMutex.withLock {
        setState {
            playListLoading = true
            playList = null
        }
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
                    e.toString().let {
                        //收藏夹内视频个数为20的整倍数时会弹，但是不影响运行结果
                        if(it != "java.lang.NullPointerException:" +
                            " Parameter specified as non-null is null:" +
                            " method kotlin.collections.CollectionsKt__IterablesKt.collectionSizeOrDefault," +
                            " parameter <this>"){
                            PopTip.show(it)
                        }
                    }
                }
                loadFinish = true
            } finally {
            }
        }
        this.setState {
            playList = PlayListInfo(
                name = mediaTitle,
                from = mediaId,
                items = items,
                type = FAVORITE,
            )
            playListLoading = false
        }
    }

    suspend fun setSeasonList(seasonId: String, seasonTitle: String, seasonIndex: Int)
    = playListLoadingMutex.withLock{
        setState {
            playListLoading = true
            playList = null
        }
        var items = listOf<PlayListItemInfo>()
        try {
            val req = bilibili.app.view.v1.SeasonReq(
                seasonId = seasonId.toLong(),
            )
            val res = BiliGRPCHttp.request {
                ViewGRPC.season(req)
            }.awaitCall()
            items = (res.season?.sections?.get(seasonIndex)?.episodes ?: listOf()).map{
                PlayListItemInfo(
                    aid = it.aid.toString(),
                    cid = it.cid.toString(),
                    duration = 0,
                    title = it.title,
                    cover = it.cover,
                    ownerId = it.author?.mid.toString(),
                    ownerName = it.author?.name.toString(),
                    from = seasonId,
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show(e.toString())
        } finally {
        }
        setState {
            playList = PlayListInfo(
                name = seasonTitle,
                from = seasonId,
                items = items,
                type = SEASON,
            )
            playListLoading = false
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

