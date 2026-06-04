package com.a10miaomiao.bilimiao.comm.store

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.Cursor
import bilibili.app.interfaces.v1.CursorV2Req
import bilibili.app.interfaces.v1.HistoryGRPC
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResponseResult
import com.a10miaomiao.bilimiao.comm.entity.bangumi.MyBangumiFollowListInfo
import com.a10miaomiao.bilimiao.comm.entity.history.ToViewInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaFoldersInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.instance

class UserLibraryStore (override val di: DI) :
    ViewModel(), BaseStore<UserLibraryStore.State> {

    data class FavouriteInfo(
        val defaultFavCount: Int = 0,
        val defaultFavId: String = "",
        val defaultFavTitle: String = "默认收藏夹",
//        val subscriptionCount: Int = 0,
    )

    data class HistoryInfo(
        val aid: Long,
        val title: String = "",
        val cover: String? = null,
        val viewAt: Long = 0,
    )

    data class WatchLaterInfo(
        val aid: Long,
        val title: String = "",
        val cover: String = "",
//        val addToAt: Long = 0,
    )

//    data class BangumiInfo(
//        val bangumiCount: Int = 0,
//        val cinemaCount: Int = 0,
//    )

    data class State (
        val favourite: FavouriteInfo = FavouriteInfo(),
        val history: List<HistoryInfo> = emptyList(),
        val watchLater: List<WatchLaterInfo> = emptyList(),
//        val bangumi: BangumiInfo = BangumiInfo(),
    )

    private val userStore: UserStore by instance()

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    override fun init(context: Context) {
        val mid = userStore.state.info?.mid
        viewModelScope.launch {
            loadHistoryData()
            if (mid != null) {
                loadWatchLaterData()
                loadFavouriteData(mid)
//                loadBangumiData()
            }
        }
    }

    private suspend fun loadHistoryData() {
        try {
            val req = CursorV2Req(
                business = "archive",
                cursor = Cursor(),
            )
            val res = BiliGRPCHttp.request {
                HistoryGRPC.cursorV2(req)
            }.awaitCall()
            stateFlow.value = state.copy(
                history = res.items
                    .filter { item -> item.business == "archive" }
                    .take(2)
                    .map { item -> HistoryInfo(
                        aid = item.kid,
                        title = item.title,
                        cover = item.cardOgv?.cover
                            ?: item.cardUgc?.cover,
                        viewAt = item.viewAt,
                    )}
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadWatchLaterData() {
        try {
            val res = BiliApiService.userApi
                .videoToview(
                    sortField = 1,
                    asc = false,
                )
                .awaitCall()
                .json<ResponseData<ToViewInfo>>()
            if (res.isSuccess) {
                val data = res.requireData()
                stateFlow.value = state.copy(
                    watchLater = data.list
                        .take(2)
                        .map {
                            WatchLaterInfo(
                                aid = it.aid,
                                title = it.title,
                                cover = it.pic,
                            )
                        }
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun loadFavouriteData(mid: Long) {
        val defaultFavResult = kotlin.runCatching {
            BiliApiService.userApi
                .favCreatedList(
                    mid.toString(),
                    pageNum = 1,
                    pageSize = 1
                )
                .awaitCall()
                .json<ResponseData<ListAndCountInfo<MediaListInfo>>>()
                .let {
                    if (it.isSuccess) {
                        it.requireData().list.firstOrNull()
                    } else {
                        null
                    }
                }
        }
//        val collectedResult = kotlin.runCatching {
//            BiliApiService.userApi
//                .favCollectedList(
//                    mid.toString(),
//                    pageNum = 1,
//                    pageSize = 1
//                )
//                .awaitCall()
//                .json<ResponseData<MediaFoldersInfo>>()
//                .let {
//                    if (it.isSuccess) {
//                        0
//                    } else {
//                        0
//                    }
//                }
//        }
        val defaultFav = defaultFavResult.getOrNull()
        stateFlow.value = state.copy(
            favourite = FavouriteInfo(
                defaultFavCount = defaultFav?.media_count ?: 0,
                defaultFavId = defaultFav?.id ?: "",
                defaultFavTitle = defaultFav?.title ?: "",
//                subscriptionCount = collectedResult.getOrElse { 0 },
            )
        )
    }

//    private suspend fun loadBangumiData() {
//        val cinemaResult = kotlin.runCatching {
//            BiliApiService.bangumiAPI
//                .followList(
//                    type = "cinema",
//                    status = 0,
//                    pageNum = 1,
//                    pageSize = 1,
//                )
//                .awaitCall()
//                .json<ResponseResult<MyBangumiFollowListInfo>>()
//                .let {
//                    if (it.isSuccess) {
//                        it.requireData().total
//                    } else {
//                        0
//                    }
//                }
//        }
//        val bangumiResult = kotlin.runCatching {
//            BiliApiService.bangumiAPI
//                .followList(
//                    type = "bangumi",
//                    status = 0,
//                    pageNum = 1,
//                    pageSize = 1,
//                )
//                .awaitCall()
//                .json<ResponseResult<MyBangumiFollowListInfo>>()
//                .let {
//                    if (it.isSuccess) {
//                        it.requireData().total
//                    } else {
//                        0
//                    }
//                }
//        }
//        stateFlow.value = state.copy(
//            bangumi = BangumiInfo(
//                bangumiCount = bangumiResult.getOrNull() ?: 0,
//                cinemaCount = cinemaResult.getOrNull() ?: 0,
//            )
//        )
//    }

    fun appendHistory(history: HistoryInfo) {
        val newHistory = state.history.toMutableList()
        newHistory.add(0, history)
        setHistoryList(newHistory)
    }

    fun appendWatchLater(watchLater: WatchLaterInfo) {
        val newWatchLater = state.watchLater.toMutableList()
        newWatchLater.add(0, watchLater)
        setWatchLaterList(newWatchLater)
    }

    fun setHistoryList(list: List<HistoryInfo>) {
        stateFlow.value = state.copy(
            history = list.take(2)
        )
    }

    fun setWatchLaterList(list: List<WatchLaterInfo>) {
        stateFlow.value = state.copy(
            watchLater = list.take(2)
        )
    }

//    fun setFavouriteCount(
//        defaultFavCount: Int = 0,
//        subscriptionCount: Int = 0,
//    ) {
//        stateFlow.value = state.copy(
//            favourite = FavouriteInfo(
//                defaultFavCount = defaultFavCount,
//                subscriptionCount = subscriptionCount,
//            )
//        )
//    }
//
//    fun setBangumiCount(
//        bangumiCount: Int = 0,
//        cinemaCount: Int = 0,
//    ) {
//        stateFlow.value = state.copy(
//            bangumi = BangumiInfo(
//                bangumiCount = bangumiCount,
//                cinemaCount = cinemaCount,
//            )
//        )
//    }


}