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


class PlayListStore(override val di: DI) :
    ViewModel(), BaseStore<PlayListStore.State> {

    data class State(
        var name: String? = null,
        var from: PlayListFrom? = null,
        var items: List<PlayListItemInfo> = listOf(),
        var loading: Boolean = false,
    ) {

        fun isEmpty(): Boolean {
            return items.isEmpty()
        }

        fun indexOfAid(aid: String): Int{
            return items.indexOfFirst {
                it.aid == aid
            }
        }

        fun indexOfCid(cid: String): Int{
            return items.indexOfFirst {
                it.cid == cid
            }
        }

        fun inListForAid(aid: String): Boolean{
            return indexOfAid(aid) != -1
        }

        fun inListForCid(cid: String): Boolean{
            return indexOfCid(cid) != -1
        }
    }

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun clearPlayList(
        loading: Boolean = false
    ) {
        this.setState {
            this.name = null
            this.from = null
            this.items = listOf()
            this.loading = loading
        }
    }

    fun setPlayList(
        name: String,
        from: PlayListFrom,
        items: List<PlayListItemInfo>,
    ) {
        this.setState {
            this.name = name
            this.from = from
            this.items = items
            this.loading = false
        }
    }

    fun setPlayList(info: UgcSeasonInfo, index: Int) {
        val sectionInfo = info.sections[index]
        val items = sectionInfo.episodes.map {
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
            info.title + "：" + sectionInfo.title
        } else {
            info.title
        }
        setPlayList(
            name = title,
            from = PlayListFrom.Section(
                seasonId = info.id,
                sectionId = sectionInfo.id,
            ),
            items = items,
        )
    }



    private val playListLoadingMutex = Mutex()
    suspend fun setFavoriteList(mediaId: String, mediaTitle: String)
            = playListLoadingMutex.withLock {
        clearPlayList(
            loading = true
        )
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
                    val newItems = result?.map {
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
                    if(newItems != null){
                        items.addAll(newItems)
                        loadFinish = newItems.size != pageSize
                    } else {
                        loadFinish = true
                    }
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
        setPlayList(
            name = mediaTitle,
            from = PlayListFrom.Favorite(
                mediaId = mediaId,
            ),
            items = items,
        )
    }

    suspend fun setSeasonList(seasonId: String, seasonTitle: String, seasonIndex: Int)
            = playListLoadingMutex.withLock{
        clearPlayList(
            loading = true
        )
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
        setPlayList(
            name = seasonTitle,
            from = PlayListFrom.Season(
                seasonId = seasonId
            ),
            items = items,
        )
    }
}

