package cn.a10miaomiao.bilimiao.compose.pages.user

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import cn.a10miaomiao.bilimiao.compose.common.defaultNavOptions
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.mine.MyFollowPage
import cn.a10miaomiao.bilimiao.compose.pages.web.WebPage
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class UserSpaceViewModel(
    override val di: DI,
    val vmid: String,
    val archiveViewModel: UserArchiveViewModel,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()
    private val pageNavigation by instance<PageNavigation>()
    val activity: AppCompatActivity by instance()
    val userStore: UserStore by instance()
    val filterStore: FilterStore by instance()

    private val _loading = MutableStateFlow(false);
    val loading: StateFlow<Boolean> get() = _loading

    private val _fail = MutableStateFlow<Any?>(null)
    val fail: StateFlow<Any?> get() = _fail

    private val _detailData = MutableStateFlow<SpaceInfo?>(null)
    val detailData: StateFlow<SpaceInfo?> get() = _detailData

    private val _isFollow = MutableStateFlow(false)
    val isFollow: StateFlow<Boolean> get() = _isFollow

    private val _isFiltered = mutableStateOf(!filterStore.filterUpper(vmid))
    val isFiltered get() = _isFiltered.value

    val isSelf get() = userStore.isSelf(vmid)

    val tabs = listOf(
        UserSpacePageTabs.Index(this),
        UserSpacePageTabs.Dynamic(vmid),
        UserSpacePageTabs.Archive(archiveViewModel),
    )

    val pagerState = PagerState{ tabs.size }
    val currentPage get() = pagerState.currentPage

    init {
        if (vmid.isNotBlank()) {
            loadData()
        }
    }

    suspend fun changeTab(index: Int, animate: Boolean = false) {
        if (animate) {
            pagerState.animateScrollToPage(index)
        } else {
            pagerState.scrollToPage(index)
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            _loading.value = true
            _fail.value = null
            val res = UserApi().space(vmid).awaitCall().gson<ResultInfo<SpaceInfo>>()
            if (res.code == 0) {
                _detailData.value = res.data
                _isFollow.value = res.data.card.relation.is_follow == 1
            } else {
                _fail.value = res.message
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            _fail.value = e
            PopTip.show("网络错误")
            e.printStackTrace()
        } finally {
            _loading.value = false
        }
    }

    fun filterUpperDelete () {
        filterStore.deleteUpper(vmid.toLong())
        _isFiltered.value = false
    }

    fun filterUpperAdd () {
        val info = detailData.value
        if (info == null) {
            PopTip.show("请等待信息加载完成")
        } else {
            filterStore.addUpper(
                info.card.mid.toLong(),
                info.card.name,
            )
            _isFiltered.value = true
        }
    }

    fun getUserSpaceUrl (): String {
        return "https://space.bilibili.com/${vmid}"
    }

    fun attention() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val data = detailData.value ?: return@launch
            val mode = if (isFollow.value) { 2 } else { 1 }
            val res = BiliApiService.userRelationApi
                .modify(vmid, mode)
                .awaitCall().gson<MessageInfo>()
            if (res.code == 0) {
                _isFollow.value = mode == 1
                PopTip.show(if (mode == 1) {
                    "关注成功"
                } else {
                    "已取消关注"
                })
            } else {
                PopTip.show(res.message)
            }
        } catch (e: Exception) {
            PopTip.show("网络错误")
            e.printStackTrace()
        }
    }

    fun toFans() {
        pageNavigation.navigate(WebPage(
            url = "https://space.bilibili.com/h5/follow?type=fans&mid=$vmid"
        ))
    }

    fun toFollow() {
        if (isSelf) {
            pageNavigation.navigate(MyFollowPage())
        } else {
            pageNavigation.navigate(UserFollowPage(vmid))
        }
    }

    fun toBangumiFollow() {
        if (isSelf) {
            pageNavigation.navigate(BangumiFollowPage())
        } else {
            pageNavigation.navigate(UserBangumiPage(vmid))
        }
    }


    fun toLikeArchive() {
        pageNavigation.navigate(UserLikeArchivePage(vmid))
    }

    fun toVideoDetail(item: SpaceInfo.ArchiveItem) {
        pageNavigation.navigateToVideoInfo(item.param)
    }

    fun toBangumiDetail(item: SpaceInfo.SeasonItem) {
        pageNavigation.navigate(BangumiDetailPage(
            id = item.param
        ))
    }

    fun toFavouriteList() {
        pageNavigation.navigate(UserFavouritePage(
            mid = vmid
        ))
    }

    fun toFavouriteDetail(item: SpaceInfo.Favourite2Item) {
        pageNavigation.navigate(UserFavouriteDetailPage(
            id = item.media_id,
            title = item.title
        ))
    }

    fun menuItemClick(view: View, item: MenuItemPropInfo) {
        when (item.key) {
            // 取消屏蔽
            1 -> filterUpperDelete()
            // 屏蔽
            2 -> filterUpperAdd()
            // 用浏览器打开
            3 -> {
                val url = getUserSpaceUrl()
                BiliUrlMatcher.toUrlLink(activity, url)
            }
            // 复制链接
            4 -> {
                val clipboard =
                    activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val label = "url"
                val text = getUserSpaceUrl()
                val clip = ClipData.newPlainText(label, text)
                clipboard.setPrimaryClip(clip)
                PopTip.show("已复制：$text")
            }
            // 分享
            5 -> {
                val info = detailData.value
                val url = getUserSpaceUrl()
                val shareIntent = Intent().also {
                    it.action = Intent.ACTION_SEND
                    it.type = "text/plain"
                    it.putExtra(Intent.EXTRA_SUBJECT, "这个UP主非常nice")
                    it.putExtra(
                        Intent.EXTRA_TEXT,
                        info?.card?.name + " " + url
                    )
                }
                activity.startActivity(Intent.createChooser(shareIntent, "分享"))
            }
            11, 12 -> {
                archiveViewModel.changeRankOrder(item.action ?: "")
            }
            MenuKeys.follow -> {
                attention()
            }
        }
    }

    fun searchSelfPage(keyword: String) {
        val name = detailData.value?.card?.name ?: return
        val nav = fragment.findNavController()
        val url = "bilimiao://user/${vmid}/search?name=${name}&text=${keyword}"
        nav.navigate(Uri.parse(url))
    }

}