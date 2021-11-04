package com.a10miaomiao.bilimiao.page.video

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.region.RegionInfo
import com.a10miaomiao.bilimiao.comm.entity.video.*
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.page.region.RegionDetailsFragment
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class VideoInfoViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val type by lazy { fragment.requireArguments().getString(MainNavGraph.args.type, "AV") }
    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }

    var info: VideoInfo? = null
    var relates = mutableListOf<VideoRelateInfo>()
    var pages = mutableListOf<VideoPageInfo>()
    var staffs = mutableListOf<VideoStaffInfo>()

    var loading = false
    var loadState = LoadMoreStatus.Loading

    var state = ""

    // 投币数量
    var coinNum = 1

    // 收藏夹列表
//    val favoriteCreatedList = MiaoList<MediaListInfo>()
//    val favoriteLoading = MiaoLiveData(false)
//    val favoriteSelectedMap = HashMap<Long, Boolean>()


    init {

    }

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                state = ""
                loading = true
            }
//            val filterStore = Store.from(context).filterStore
            val res = BiliApiService.videoAPI
                .info(id, type = type)
                .call()
                .gson<ResultInfo<VideoInfo>>()
            if (res.code == 0) {
                val data = res.data
//                data.desc = BiliUrlMatcher.customString(data.desc)
                val relatesData = data.relates ?: listOf()
                val staffData = data.staff ?: listOf()
 //                val relatesData = data.relates.filter {
//                    filterStore.filterWord(it.title)
//                            && it.owner != null
//                            && filterStore.filterUpper(it.owner.mid)
//                }
                val pagesData = data.pages.map {
                    it.part = if (it.part.isNotEmpty()) {
                        it.part
                    } else {
                        data.title
                    }
                    it
                }
                ui.setState {
                    info = data
                    relates = relatesData.toMutableList()
                    pages = pagesData.toMutableList()
                    staffs = staffData.toMutableList()
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

}