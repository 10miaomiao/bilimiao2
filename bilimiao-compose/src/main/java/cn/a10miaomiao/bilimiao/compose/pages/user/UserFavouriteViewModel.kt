package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.pages.user.components.FavouriteEditDialogState
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaFoldersInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

internal class UserFavouriteViewModel(
    override val di: DI,
    val mid: String,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val userStore: UserStore by instance()

    private val playerStore by instance<PlayerStore>()
    private val playListStore by instance<PlayListStore>()

    val createdList = FlowPaginationInfo<MediaListInfo>(
        pageSize = 10
    )
    val createdListIsRefreshing = MutableStateFlow(false)

    val collectedList = FlowPaginationInfo<MediaListInfo>(
        pageSize = 10
    )
    val collectedListIsRefreshing = MutableStateFlow(false)

    val openedMedia = MutableStateFlow<MediaListInfo?>(null)

    val editDialogState = MutableStateFlow<FavouriteEditDialogState?>(null)

    val version = MutableStateFlow(0)

    init {
        loadData(UserFavouriteFolderType.Created, 1)
        loadData(UserFavouriteFolderType.Collected, 1)
    }

    fun getListAndIsRefreshingFlow(
        type: UserFavouriteFolderType,
    ) = if (type == UserFavouriteFolderType.Created) {
        Pair(createdList, createdListIsRefreshing)
    } else {
        Pair(collectedList, collectedListIsRefreshing)
    }

    fun loadData(
        type: UserFavouriteFolderType,
        pageNum: Int,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val (list, isRefreshing) = getListAndIsRefreshingFlow(type)
        try {
            list.loading.value = true
            val hasMore: Boolean
            val resultList = if (type == UserFavouriteFolderType.Created) {
                val res = BiliApiService.userApi.favCreatedList(
                    mid,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                ).awaitCall().json<ResponseData<ListAndCountInfo<MediaListInfo>>>()
                if (!res.isSuccess) {
                    list.fail.value = res.message
                    return@launch
                }
                val result = res.requireData()
                hasMore = result.has_more
                result.list
            } else {
                val res = BiliApiService.userApi.favCollectedList(
                    mid,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                ).awaitCall().json<ResponseData<MediaFoldersInfo>>()
                if (!res.isSuccess) {
                    list.fail.value = res.message
                    return@launch
                }
                val result = res.requireData()
                hasMore = result.has_more
                result.folders.map {
                    it.folder_detail
                }
            }
            if (pageNum == 1) {
                list.data.value = resultList
            } else {
                list.data.value = mutableListOf<MediaListInfo>().apply {
                    addAll(list.data.value)
                    addAll(resultList)
                }
            }
            list.finished.value = !hasMore
            list.pageNum = pageNum
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = "无法连接到御坂网络"
        } finally {
            list.loading.value = false
            isRefreshing.value = false
        }
    }

    fun tryAgainLoadData(type: UserFavouriteFolderType) {
        val (list, _) = getListAndIsRefreshingFlow(type)
        loadData(type, list.pageNum)
    }

    fun refresh(type: UserFavouriteFolderType) {
        val (list, isRefreshing) = getListAndIsRefreshingFlow(type)
        isRefreshing.value = true
        list.finished.value = false
        list.fail.value = ""
        loadData(type, 1)
    }

    fun loadMore(type: UserFavouriteFolderType) {
        val (list, _) = getListAndIsRefreshingFlow(type)
        if (!list.finished.value && !list.loading.value) {
            loadData(type, list.pageNum + 1)
        }
    }

    fun openMediaDetail(media: MediaListInfo) {
        openedMedia.value = media
    }

    fun closeMediaDetail() {
        openedMedia.value = null
    }

    fun updateOpenedMedia(
        mediaId: String,
        title: String,
        cover: String,
        intro: String,
        privacy: Int, // 0:公开,1:不公开
    ) {
        val index = createdList.data.value.indexOfFirst {
            it.id == mediaId
        }
        if (index >= 0) {
            val newList = createdList.data.value.toMutableList()
            val updateItem = newList[index]
            val attr = if (privacy == 0) {
                updateItem.attr and 1.inv()
            } else {
                updateItem.attr or 1
            }
            newList[index] = updateItem.copy(
                title = title,
                cover = cover,
                intro = intro,
                attr = attr,
            )
            createdList.data.value = newList
            openedMedia.value = newList[index]
        }
    }

    fun updateOpenedSeason(
        seasonId: String,
        favState: Int,
    ) {
        val index = collectedList.data.value.indexOfFirst {
            it.id == seasonId
        }
        if (index >= 0) {
            val newList = collectedList.data.value.toMutableList()
            val updateItem = newList[index]
            newList[index] = updateItem.copy(
                fav_state = favState,
            )
            collectedList.data.value = newList
            openedMedia.value = newList[index]
        }
    }

    suspend fun addFolder(
        title: String,
        cover: String,
        intro: String,
        privacy: Int, // 0:公开,1:不公开
    ) {
        val res = BiliApiService.userApi
            .favAddFolder(
                title = title,
                cover = cover,
                intro = intro,
                privacy = privacy,
            )
            .awaitCall()
            .json<MessageInfo>()
        if (!res.isSuccess) {
            throw Exception(res.message)
        }
        refresh(UserFavouriteFolderType.Created)
    }
}