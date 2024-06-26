package com.a10miaomiao.bilimiao.page.video

import android.content.Context
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.datastore.SettingConstants
import com.a10miaomiao.bilimiao.comm.datastore.SettingPreferences
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate
import com.a10miaomiao.bilimiao.comm.delegate.player.VideoPlayerSource
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.player.PlayListFrom
import com.a10miaomiao.bilimiao.comm.entity.video.UgcSeasonInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoPageInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoRelateInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoStaffInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoTagInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.store.PlayListStore
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.widget.scaffold.getScaffoldView
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoInfoViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val basePlayerDelegate by instance<BasePlayerDelegate>()
    val scaffoldApp by lazy { fragment.requireActivity().getScaffoldView() }

    //    val type by lazy { fragment.requireArguments().getString(MainNavArgs.type, "AV") }
    var id: String = ""
    var info: VideoInfo? = null
    var relates = mutableListOf<VideoRelateInfo>()
    var pages = mutableListOf<VideoPageInfo>()
    var ugcSeason: UgcSeasonInfo? = null
    var ugcSeasonEpisodes = mutableListOf<Any>() // UgcSeasonInfo | UgcEpisodeInfo
    var staffs = mutableListOf<VideoStaffInfo>()
    var tags = mutableListOf<VideoTagInfo>()

    var loading = false
    var loadState = LoadMoreStatus.Loading

    var state = ""

    val filterStore: FilterStore by instance()

    val playerStore: PlayerStore by instance()

    val playListStore: PlayListStore by instance()

    init {
        val arguments = fragment.requireArguments()
        id = arguments.getString(MainNavArgs.id, "")
        loadData()
    }

    fun changeVideo(aid: String) {
        if (aid == id) {
            return
        }
        ui.setState {
            id = aid
            loadData()
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                state = ""
                loading = true
            }
            val type = if (id.indexOf("BV") == 0) {
                VideoInfoFragment.TYPE_BV
            } else {
                VideoInfoFragment.TYPE_AV
            }
            val res = BiliApiService.videoAPI
                .info(id, type = type)
                .awaitCall()
                .gson<ResultInfo<VideoInfo>>()
            if (res.code == 0) {
                val data = res.data
                data.desc = BiliUrlMatcher.customString(data.desc)
                val staffData = data.staff ?: listOf()
                val tagData = data.tag ?: listOf()
                val relatesData = data.relates?.filter {
                    filterStore.filterWord(it.title)
                            && filterStore.filterUpper(it.owner?.mid ?: "-1")
                            && filterStore.filterTag(it.param, it.goto)
                } ?: emptyList()
                val pagesData = data.pages.map {
                    it.part = if (it.part.isNotEmpty()) {
                        it.part
                    } else {
                        data.title
                    }
                    it
                }
                val ugcSeasonData = data.ugc_season
                ui.setState {
                    info = data
                    relates = relatesData.filterNot { it.aid.isNullOrEmpty() }.toMutableList()
                    pages = pagesData.toMutableList()
                    staffs = staffData.toMutableList()
                    tags = tagData.toMutableList()
                    if (ugcSeasonData != null
                        && ugcSeasonData.sections.isNotEmpty()
                    ) {
                        val sections = ugcSeasonData.sections
                        ugcSeason = ugcSeasonData
                        ugcSeasonEpisodes = mutableListOf()
                        if (sections.size == 1) {
                            ugcSeasonEpisodes.addAll(sections[0].episodes)
                        } else {
                            ugcSeasonData.sections.forEachIndexed { index, ugcSectionInfo ->
                                val episodes = ugcSectionInfo.episodes
                                ugcSeasonEpisodes.add(ugcSectionInfo)
                                ugcSeasonEpisodes.addAll(episodes)
                            }
                        }

                    }
                }
                withContext(Dispatchers.Main) {
                    if (!jumpSeason(data)) {
                        autoStartPlay(data)
                    }
                }
            } else {
                ui.setState {
                    state = if (res.code == -403) {
                        "绝对领域，拒绝访问＞﹏＜"
                    } else {
                        res.message
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                state = "无法连接到御坂网络"
            }
        } finally {
            ui.setState { loading = false }
        }
    }

    private fun autoStartPlay(info: VideoInfo) = viewModelScope.launch {
        val openMode = SettingPreferences.mapData(fragment.requireActivity()) {
            it[PlayerOpenMode] ?: SettingConstants.PLAYER_OPEN_MODE_DEFAULT
        }
        if (scaffoldApp.showPlayer) {
            if (basePlayerDelegate.isPlaying()) {
                // 自动替换正在播放的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE != 0) {
                    playVideo(info, 0)
                }
            } else if (basePlayerDelegate.isPause()) {
                // 自动替换暂停的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE != 0) {
                    playVideo(info, 0)
                }
            } else {
                // 自动替换完成的视频
                if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE != 0) {
                    playVideo(info, 0)
                }
            }
        } else {
            // 自动播放新视频
            if (openMode and SettingConstants.PLAYER_OPEN_MODE_AUTO_PLAY != 0) {
                playVideo(info, 0)
            }
        }
    }

    private fun playVideo(info: VideoInfo, page: Int) {
        val videoPages = pages
        val aid = info.aid
        val title = videoPages[page].part
        val cid = videoPages[page].cid
        if (basePlayerDelegate.getSourceIds().aid == aid) {
            // 同个视频不替换播放
            return
        }
        basePlayerDelegate.openPlayer(
            VideoPlayerSource(
                mainTitle = info.title,
                title = title,
                coverUrl = info.pic,
                aid = info.aid,
                id = cid,
                ownerId = info.owner.mid,
                ownerName = info.owner.name,
            ).apply {
                pages = videoPages.map {
                    VideoPlayerSource.PageInfo(
                        cid = it.cid,
                        title = it.part,
                    )
                }
            }
        )
    }

    /**
     * 跳转番剧
     */
    private fun jumpSeason(info: VideoInfo): Boolean {
        info.season?.let {
            if (it.is_jump == 1) {
                val nav = fragment.findNavController()
                val previousId = nav.previousBackStackEntry?.destination?.id
                nav.navigateToCompose(
                    BangumiDetailPage(),
                    navOptions {
                        previousId?.let(::popUpTo)
                    }
                ) {
                    id set it.season_id
                }
                return true
            }
        }
        return false
    }

    /**
     * 点赞/取消点赞
     */
    fun requestLike() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val curInfo = info ?: return@launch
            val res = BiliApiService.videoAPI
                .like(
                    aid = curInfo.aid,
                    dislike = curInfo.req_user.dislike ?: 0,
                    like = curInfo.req_user.like ?: 0,
                )
                .awaitCall()
                .gson<MessageInfo>()
            if (res.code == 0) {
                ui.setState {
                    val reqUser = curInfo.req_user.copy()
                    val stat = curInfo.stat.copy()
                    if (reqUser.like == 1) {
                        reqUser.like = null
                        stat.like--
                    } else {
                        reqUser.like = 1
                        reqUser.dislike = null
                        stat.like++
                    }
                    curInfo.req_user = reqUser
                    curInfo.stat = stat
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.toString())
            }
        }
    }

    /**
     * 投币
     */
    fun requestCoin(coinNum: Int) = viewModelScope.launch(Dispatchers.IO) {
        try {
            val curInfo = info ?: return@launch
            val res = BiliApiService.videoAPI
                .coin(curInfo.aid, coinNum)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.code == 0) {
                ui.setState {
                    val reqUser = curInfo.req_user.copy()
                    val stat = curInfo.stat.copy()
                    stat.coin += coinNum
                    reqUser.coin = coinNum
                    curInfo.req_user = reqUser
                    curInfo.stat = stat
                }
                withContext(Dispatchers.Main) {
                    PopTip.show("感谢投币")
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.toString())
            }
        }
    }

    /**
     * 收藏
     */
    fun requestFavorite(
        favIds: List<String>,
        addIds: List<String>,
        delIds: List<String>,
    ) = viewModelScope.launch(Dispatchers.IO) {
        val curInfo = info ?: return@launch
        try {
            val res = BiliApiService.videoAPI
                .favoriteDeal(
                    aid = curInfo.aid,
                    addIds = addIds,
                    delIds = delIds,
                )
                .awaitCall()
                .gson<MessageInfo>()
            if (res.code == 0) {
                ui.setState {
                    val reqUser = curInfo.req_user.copy()
                    val stat = curInfo.stat.copy()
                    if (favIds.size - delIds.size + addIds.size == 0) {
                        stat.favorite--
                        reqUser.favorite = null
                    } else {
                        if (favIds.isEmpty()) {
                            stat.favorite++
                        }
                        reqUser.favorite = 1
                    }
                    curInfo.req_user = reqUser
                    curInfo.stat = stat
                }

                //收藏夹变动，重新加载播放列表
                val playListFrom = playListStore.state.from
                val playListName = playListStore.state.name
                if (playListFrom is PlayListFrom.Favorite && playListName != null) {
                    //当前列表为收藏夹类型
                    val currentId = playListFrom.mediaId
                    if (addIds.contains(currentId) || delIds.contains(currentId)) {
                        if (delIds.contains(currentId) && curInfo.aid == playerStore.state.aid) {
                            //从收藏夹中删除的是当前播放的视频
                            playListStore.clearPlayList()
                        } else {
                            playListStore.setFavoriteList(currentId, playListName)
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.toString())
            }
        }
    }


    /**
     * 添加至稍后再看
     */
    fun addVideoHistoryToview() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val curInfo = info ?: return@launch
            val res = BiliApiService.userApi
                .videoHistoryToviewAdd(curInfo.aid)
                .awaitCall()
                .gson<MessageInfo>()
            if (res.code == 0) {
                withContext(Dispatchers.Main) {
                    PopTip.show("已添加至稍后再看")
                }
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                PopTip.show(e.toString())
            }
        }
    }

//    data class SeasonEpisodeInfo(
//
//    )
}