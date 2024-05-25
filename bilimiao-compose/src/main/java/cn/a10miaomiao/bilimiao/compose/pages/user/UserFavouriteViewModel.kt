package cn.a10miaomiao.bilimiao.compose.pages.user

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.navigate
import cn.a10miaomiao.bilimiao.compose.comm.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.comm.navigation.findComposeNavController
import cn.a10miaomiao.bilimiao.compose.pages.playlist.PlayListPage
import com.a10miaomiao.bilimiao.comm.entity.ListAndCountInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaFoldersInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class UserFavouriteViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    private val playerStore by instance<PlayerStore>()

    val createdList = FlowPaginationInfo<MediaListInfo>(
        pageSize = 10
    )
    val createdListIsRefreshing = MutableStateFlow(false)

    val collectedList = FlowPaginationInfo<MediaListInfo>(
        pageSize = 10
    )
    val collectedListIsRefreshing = MutableStateFlow(false)

    val openedMedia = MutableStateFlow<MediaListInfo?>(null)

    var mid = "0"
        set(value) {
            if (field != value) {
                field = value
                loadData( UserFavouriteFolderType.Created, 1)
                loadData( UserFavouriteFolderType.Collected, 1)
            }
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
            val resultList = if(type == UserFavouriteFolderType.Created) {
                val res = BiliApiService.userApi.favCreatedList(
                    mid,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                ).awaitCall().gson<ResultInfo<ListAndCountInfo<MediaListInfo>>>()
                if (!res.isSuccess) {
                    list.fail.value = res.message
                    return@launch
                }
                val result = res.data
                hasMore = result.has_more
                result.list
            } else {
                val res = BiliApiService.userApi.favCollectedList(
                    mid,
                    pageNum = pageNum,
                    pageSize = list.pageSize
                ).awaitCall().gson<ResultInfo<MediaFoldersInfo>>()
                if (!res.isSuccess) {
                    list.fail.value = res.message
                    return@launch
                }
                val result = res.data
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

    fun toPlayList() {
        openedMedia.value?.let {
            viewModelScope.launch {
                if(it.type == 21){
                    //合集
                    playerStore.setSeasonList(it.id,it.title,0)
                } else {
                    //收藏
                    playerStore.setFavoriteList(it.id, it.title)
                }
            }
        }
        val nav = fragment.findComposeNavController()
        nav.navigate(PlayListPage())
    }
}