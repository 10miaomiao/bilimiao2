package cn.a10miaomiao.bilimiao.compose.pages.user

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.entity.FlowPaginationInfo
import cn.a10miaomiao.bilimiao.compose.common.localContainerView
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.input.SearchBox
import cn.a10miaomiao.bilimiao.compose.components.list.ListStateBox
import cn.a10miaomiao.bilimiao.compose.components.user.UserInfoCard
import com.a10miaomiao.bilimiao.comm.entity.ResponseData
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.json
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.a10miaomiao.bilimiao.store.WindowStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Serializable
class SearchFollowPage : ComposePage() {

    @Composable
    override fun Content() {
        val viewModel: SearchFollowPageViewModel = diViewModel()
        SearchFollowPageContent(viewModel)
    }
}

private class SearchFollowPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val activity by instance<Activity>()
    private val pageNavigation by instance<PageNavigation>()
    private val userStore by instance<UserStore>()

    val searchText = MutableStateFlow("")
    val isRefreshing = MutableStateFlow(false)
    val list = FlowPaginationInfo<FollowingItemInfo>()

    init {
        viewModelScope.launch {
            searchText.collect {
                if (!list.loading.value) {
                    loadData(it)
                }
            }
        }
    }

    fun loadData(
        name: String,
        pageNum: Int = 1,
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val mid = userStore.state.info?.mid ?: return@launch
            list.loading.value = true
            val res = BiliApiService.userRelationApi
                .search(
                    mid = mid.toString(),
                    name = name,
                    pageNum = pageNum,
                    pageSize = list.pageSize,
                )
                .awaitCall()
                .json<ResponseData<FollowingsInfo>>()
            if (res.isSuccess) {
                list.pageNum = pageNum
                list.finished.value = res.requireData().list.isEmpty()
                if (pageNum == 1) {
                    list.data.value = res.requireData().list
                } else {
                    list.data.value = mutableListOf<FollowingItemInfo>().apply {
                        addAll(list.data.value)
                        addAll(res.requireData().list)
                    }
                }
                list.finished.value = res.requireData().list.size < list.pageSize
            } else {
                list.fail.value = res.message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            list.fail.value = e.message ?: e.toString()
        } finally {
            list.loading.value = false
            isRefreshing.value = false
            if (name != searchText.value) {
                tryAgainLoadData(searchText.value)
            }
        }
    }

    fun tryAgainLoadData(
        name: String = searchText.value
    ) {
        loadData(name)
    }

    fun updateSearchText(value: String) {
        searchText.value = value
    }

    fun toUserDetailPage(id: String) {
        pageNavigation.navigate(UserSpacePage(id))
    }

}


@Composable
private fun SearchFollowPageContent(
    viewModel: SearchFollowPageViewModel
) {
    PageConfig(
        title = "搜索我的关注"
    )
    val windowStore: WindowStore by rememberInstance()
    val windowState = windowStore.stateFlow.collectAsState().value
    val windowInsets = windowState.getContentInsets(localContainerView())

    val searchText by viewModel.searchText.collectAsState()
    val list by viewModel.list.data.collectAsState()
    val listLoading by viewModel.list.loading.collectAsState()
    val listFinished by viewModel.list.finished.collectAsState()
    val listFail by viewModel.list.fail.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(10.dp)
            .padding(top = windowInsets.topDp.dp)
    ) {
        SearchBox(
            value = searchText,
            onValueChange = viewModel::updateSearchText,
            modifier = Modifier.height(40.dp)
                .fillMaxWidth(),
            placeholder = {
                Text("搜索我的关注")
            }
        )
        Box(
            modifier = Modifier.weight(1f)
        ) {
            if (listLoading) {
                ListStateBox(loading = true)
            } else if (listFail.isNotBlank()) {
                ListStateBox(
                    fail = listFail,
                    loadMore = viewModel::tryAgainLoadData
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(400.dp),
                    modifier = Modifier.padding(
                        start = windowInsets.leftDp.dp,
                        end = windowInsets.rightDp.dp,
                    )
                ) {
                    items(list.size, { list[it].mid }) {
                        val item = list[it]
                        Box(
                            modifier = Modifier.padding(vertical = 5.dp),
                        ) {
                            UserInfoCard(
                                name = item.uname,
                                face = item.face,
                                sign = item.sign,
                                onClick = {
                                    viewModel.toUserDetailPage(item.mid)
                                },
                                actionContent = {}
                            )
                        }
                    }
                    item(
                        span = {
                            GridItemSpan(maxLineSpan)
                        }
                    ) {
                        Spacer(modifier = Modifier.height(windowInsets.bottomDp.dp))
                    }
                }
            }

        }
    }

}