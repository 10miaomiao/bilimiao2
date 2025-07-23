package com.a10miaomiao.bilimiao.comm.store

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.dynamic.v2.ThreePointType
import bilibili.app.view.v1.ViewGRPC
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaResponseV2Info
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListItemInfo.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
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

        fun indexOfAid(aid: String): Int {
            return items.indexOfFirst {
                it.aid == aid
            }
        }

        fun indexOfCid(cid: String): Int {
            return items.indexOfFirst {
                it.cid == cid
            }
        }

        fun inListForAid(aid: String): Boolean {
            return indexOfAid(aid) != -1
        }

        fun inListForCid(cid: String): Boolean {
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

    fun addItem(
        item: PlayListItemInfo,
        index: Int = -1,
    ) {
        if (state.items.isEmpty()) {
            return
        }
        val items = state.items.filter {
            // 去重
            it.cid != item.cid
        }.toMutableList()
        if (index == -1) {
            items.add(item)
        } else {
            items.add(index, item)
        }
        this.setState {
            this.items = items
        }
    }

    fun moveItem(fromIndex: Int, toIndex: Int) {
        if (fromIndex == toIndex ||
            fromIndex < 0 || fromIndex >= state.items.size ||
            toIndex < 0 || toIndex >= state.items.size) {
            return
        }
        val items = state.items.toMutableList()
        val item = items.removeAt(fromIndex)
        items.add(toIndex, item)
        this.setState {
            this.items = items
        }
    }

    fun removeItems(keys: Set<String>) {
        val originalItems = state.items.toMutableList()
        this.setState {
            this.items = originalItems.filter {
                !keys.contains(it.cid)
            }
        }
        PopTip.show("已移除选中视频", "恢复")
            .showLong()
            .setButton { _, _ ->
                this.setState {
                    this.items = originalItems
                }
                false
            }
    }

    fun setPlayList(info: UgcSeasonInfo, index: Int) {
        val sectionInfo = info.sections[index]
        val listFrom = PlayListFrom.Section(
            seasonId = info.id,
            sectionId = sectionInfo.id,
        )
        val currentFrom = state.from
        if (currentFrom is PlayListFrom.Section
            && currentFrom.seasonId == listFrom.seasonId
            && currentFrom.sectionId == listFrom.sectionId) {
            return
        }
        val items = sectionInfo.episodes.map {
            PlayListItemInfo(
                aid = it.aid,
                cid = it.cid,
                duration = 0,
                title = it.title,
                cover = it.cover,
                ownerId = it.author?.mid.toString(),
                ownerName = it.author?.name.toString(),
                from = listFrom,
            )
        }
        val title = if (info.sections.size > 1) {
            info.title + "：" + sectionInfo.title
        } else {
            info.title
        }
        setPlayList(
            name = title,
            from = listFrom,
            items = items,
        )
    }

    fun setPlayList(season: bilibili.app.view.v1.UgcSeason, index: Int) {
        val sectionInfo = season.sections[index]
        val listFrom = PlayListFrom.Section(
            seasonId = season.id.toString(),
            sectionId = sectionInfo.id.toString(),
        )
        val currentFrom = state.from
        if (currentFrom is PlayListFrom.Section
            && currentFrom.seasonId == listFrom.seasonId
            && currentFrom.sectionId == listFrom.sectionId) {
            return
        }
        val items = sectionInfo.episodes.map {
            PlayListItemInfo(
                aid = it.aid.toString(),
                cid = it.cid.toString(),
                duration = 0,
                title = it.title,
                cover = it.cover,
                ownerId = it.author?.mid.toString(),
                ownerName = it.author?.name.toString(),
                from = listFrom,
            )
        }
        val title = if (season.sections.size > 1) {
            season.title + "：" + sectionInfo.title
        } else {
            season.title
        }
        setPlayList(
            name = title,
            from = listFrom,
            items = items,
        )
    }

    private val playListLoadingMutex = Mutex()

    fun setFavoriteList(mediaId: String, mediaTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _setFavoriteList(mediaId, mediaTitle)
        }
    }
    private suspend fun _setFavoriteList(mediaId: String, mediaTitle: String) =
        playListLoadingMutex.withLock {
            val listFrom = PlayListFrom.Favorite(
                mediaId = mediaId,
            )
            val currentFrom = state.from
            if (currentFrom is PlayListFrom.Favorite
                && currentFrom.mediaId == mediaId
            ) {
                return@withLock
            }
            clearPlayList(
                loading = true
            )
            val items = mutableListOf<PlayListItemInfo>()
            val pageSize = 20
            var pageNum = 1
            var loadFinish = false
            while (!loadFinish) {
                try {
                    val res = BiliApiService.userApi.mediaDetail(
                        media_id = mediaId,
                        keyword = "",
                        pageNum = pageNum,
                        pageSize = 20,
                    ).awaitCall().json<ResponseData<MediaDetailInfo>>()
                    if (res.code == 0) {
                        val result = res.requireData()
                        val newItems = result.medias?.filter {
                            it.ugc != null
                        }?.map {
                            PlayListItemInfo(
                                aid = it.id,
                                cid = it.ugc!!.first_cid,
                                duration = it.duration.toInt(),
                                title = it.title,
                                cover = it.cover,
                                ownerId = it.upper.mid,
                                ownerName = it.upper.name,
                                from = listFrom,
                            )
                        } ?: emptyList()
                        if (newItems != null) {
                            items.addAll(newItems)
                            loadFinish = !result.has_more || newItems.isEmpty()
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
                    loadFinish = true
                }
            }
            setPlayList(
                name = mediaTitle,
                from = listFrom,
                items = items,
            )
        }

    fun setSeasonList(seasonId: String, seasonTitle: String, seasonIndex: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            _setSeasonList(seasonId, seasonTitle, seasonIndex)
        }
    }

    private suspend fun _setSeasonList(seasonId: String, seasonTitle: String, seasonIndex: Int) =
        playListLoadingMutex.withLock {
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
                val season = res.season
                val section = season?.sections?.get(seasonIndex)
                val listFrom = PlayListFrom.Section(
                    seasonId = season?.id.toString(),
                    sectionId = section?.id.toString(),
                )
                items = (section?.episodes ?: listOf()).map {
                    PlayListItemInfo(
                        aid = it.aid.toString(),
                        cid = it.cid.toString(),
                        duration = 0,
                        title = it.title,
                        cover = it.cover,
                        ownerId = it.author?.mid.toString(),
                        ownerName = it.author?.name.toString(),
                        from = listFrom,
                    )
                }
                setPlayList(
                    name = seasonTitle,
                    from = listFrom,
                    items = items,
                )
            } catch (e: Exception) {
                e.printStackTrace()
                PopTip.show(e.toString())
            }
        }

    fun setMedialistList(bizId: String, bizType: String, bizTitle: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _setMedialistList(bizId, bizType, bizTitle)
        }
    }
    private suspend fun _setMedialistList(bizId: String, bizType: String, bizTitle: String) =
        playListLoadingMutex.withLock {
            val listFrom = PlayListFrom.Medialist(
                bizId = bizId,
                bizType = bizType,
            )
            val currentFrom = state.from
            if (currentFrom is PlayListFrom.Medialist
                && currentFrom.bizId == bizId
                && currentFrom.bizType == bizType
            ) {
                return@withLock
            }
            clearPlayList(
                loading = true
            )
            val items = mutableListOf<PlayListItemInfo>()
            var lastOid = ""
            var loadFinish = false
            while (!loadFinish) {
                try {
                    val res = BiliApiService.userApi
                        .medialistResourceList(
                            bizId = bizId,
                            type = bizType,
                            oid = lastOid,
                        )
                        .awaitCall()
                        .json<ResponseData<MediaResponseV2Info>>()
                    if (res.code == 0) {
                        val mediaList = res.requireData().media_list ?: break
                        lastOid = mediaList.lastOrNull()?.id ?: ""
                        val newItems = mediaList.map {
                            PlayListItemInfo(
                                aid = it.id,
                                cid = it.pages[0].id,
                                duration = it.duration.toInt(),
                                title = it.title,
                                cover = it.cover,
                                ownerId = it.upper.mid,
                                ownerName = it.upper.name,
                                videoPages = it.pages.map {
                                    VideoPageInfo(
                                        cid = it.id,
                                        page = it.page,
                                        part = it.title,
                                        duration = it.duration,
                                    )
                                },
                                from = listFrom,
                            )
                        }.filter { item ->
                            items.indexOfFirst { it.aid == item.aid } == -1
                        }
                        items.addAll(newItems)
                        loadFinish = !res.requireData().has_more
                    } else {
                        PopTip.show(res.message)
                        loadFinish = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    loadFinish = true
                }
            }
            setPlayList(
                name = bizTitle,
                from = listFrom,
                items = items,
            )
        }

    fun setToviewList(
        sortField: Int, // 1全部, 10未看完
        asc: Boolean = false,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            _setToviewList(sortField, asc)
        }
    }
    private suspend fun _setToviewList(
        sortField: Int, // 1全部, 10未看完
        asc: Boolean = false,
    ) =  playListLoadingMutex.withLock {
        clearPlayList(
            loading = true
        )
        val listFrom = PlayListFrom.Toview(sortField, asc)
        val items = mutableListOf<PlayListItemInfo>()
        var startKey = ""
        var loadFinish = false
        while (!loadFinish) {
            try {
                val res = BiliApiService.userApi
                    .videoToview(
                        sortField = sortField,
                        asc = asc,
                        startKey = startKey,
                    )
                    .awaitCall()
                    .json<ResponseData<ToViewInfo>>()
                if (res.code == 0) {
                    val data = res.requireData()
                    val listData = data.list
                    val newItems = listData.map {
                        val page = it.page
                        PlayListItemInfo(
                            aid = it.aid.toString(),
                            cid = it.cid.toString(),
                            duration = it.duration,
                            title = it.title,
                            cover = it.pic,
                            ownerId = it.owner.mid,
                            ownerName = it.owner.name,
                            videoPages = if (page == null) {
                                emptyList()
                            } else {
                                listOf(
                                    VideoPageInfo(
                                        cid = page.cid,
                                        page = page.page,
                                        part = page.part,
                                        duration = page.duration,
                                    )
                                )
                            },
                            from = listFrom,
                        )
                    }.filter { item ->
                        item.videoPages.isNotEmpty()
                                && items.indexOfFirst { it.aid == item.aid } == -1
                    }
                    items.addAll(newItems)
                    loadFinish = !data.has_more || listData.isEmpty() || data.next_key.isBlank()
                    startKey = data.next_key
                } else {
                    PopTip.show(res.message)
                    loadFinish = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadFinish = true
            }
        }
        setPlayList(
            name = "稍后再看",
            from = listFrom,
            items = items,
        )
    }

    fun bilibili.app.archive.v1.Arc.toPlayListItem(
        viewPages: List<bilibili.app.view.v1.ViewPage>,
    ): PlayListItemInfo {
        val from = PlayListFrom.Video(
            aid = aid.toString(),
        )
        return PlayListItemInfo(
            aid = aid.toString(),
            cid = firstCid.toString(),
            title = title,
            cover = pic,
            duration = duration.toInt(),
            ownerId = author!!.mid.toString(),
            ownerName = author.name,
            from = from,
            videoPages = viewPages.mapNotNull {
                it.page
            }.map {
                PlayListItemInfo.VideoPageInfo(
                    cid = it.cid.toString(),
                    page = it.page,
                    part = it.part,
                    duration = it.duration.toInt(),
                )
            }
        )
    }
}

