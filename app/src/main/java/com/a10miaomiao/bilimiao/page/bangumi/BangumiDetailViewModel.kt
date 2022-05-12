package com.a10miaomiao.bilimiao.page.bangumi

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.delegate.player.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.entity.MessageInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo2
import com.a10miaomiao.bilimiao.comm.entity.bangumi.BangumiInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.EpisodeInfo
import com.a10miaomiao.bilimiao.comm.entity.bangumi.SeasonInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.ToastInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.PlayerStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import java.lang.Exception

class BangumiDetailViewModel (
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    private val myPage: MyPage by instance()

    private val playerStore by instance<PlayerStore>()

    var id: String = fragment.requireArguments().getString(MainNavGraph.args.id, "")

    var detailInfo: BangumiInfo? = null
    var loading = false
    var episodes = mutableListOf<EpisodeInfo>()
    var seasons = listOf<SeasonInfo>()
    var seasonsIndex = -1

    val isFollow get() = detailInfo?.user_status?.follow == 1

    init {
        loadData()
        viewModelScope.launch {
            playerStore.connectUi(ui)
        }
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                loading = true
                detailInfo = null
                seasonsIndex = -1
                episodes = mutableListOf()
            }
            val res = BiliApiService.bangumiAPI.seasonInfo(id)
                .awaitCall()
                .gson<ResultInfo2<BangumiInfo>>()
            if (res.code == 0) {
                val result = res.result
                ui.setState {
                    detailInfo = result
                    seasons = result.seasons
                    seasonsIndex = result.seasons.indexOfFirst { it.season_id == id }
                    episodes = result.episodes.toMutableList()
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                context.toast("无法连接到御坂网络")
            }
        } finally {
            ui.setState {
                loading = false
            }
            withContext(Dispatchers.Main) {
                myPage.pageConfig.notifyConfigChanged()
            }
            DebugMiao.log("isFollow", detailInfo?.user_status)
            DebugMiao.log("isFollow", isFollow)
        }
    }

    fun followSeason() = viewModelScope.launch(Dispatchers.IO) {
        try {
            val detailInfo = detailInfo ?: return@launch
            val mode = if (isFollow) { 2 } else { 1 }
            val res = (if (mode == 2) {
                BiliApiService.bangumiAPI.cancelFollowSeason(id)
            } else {
                BiliApiService.bangumiAPI.followSeason(id)
            }).awaitCall().gson<ResultInfo2<ToastInfo>>()
            DebugMiao.log(res)
            if (res.isSuccess) {
                detailInfo.user_status.follow = 2 - mode
                withContext(Dispatchers.Main) {
                    myPage.pageConfig.notifyConfigChanged()
                    context.toast(if (mode == 1) {
                        res.result?.toast ?: "追番成功"
                    } else {
                        res.result?.toast ?: "已取消追番"
                    })
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                context.toast("网络错误")
            }
            e.printStackTrace()
        }
    }

    fun refreshData () {
//        ui.setState {
//            list = PaginationInfo()
//            triggered = true
            loadData()
//        }
    }

    fun updateSeasonsIndex(index: Int) {
        if (seasonsIndex != index) {
            id = seasons[index].season_id
            fragment.requireArguments().putString(MainNavGraph.args.id, id)
            loadData()
            ui.setState {
                seasonsIndex = index
            }
        }
    }

    fun isPlaying (epid: String): Boolean {
        val info = playerStore.state.info
        return info.type == PlayerSourceInfo.BANGUMI && info.epid == epid
    }
}