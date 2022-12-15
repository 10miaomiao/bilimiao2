package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.middleware.v1.Preload
import bilibili.app.interfaces.v1.HistoryGrpc
import bilibili.app.interfaces.v1.HistoryOuterClass
import bilibili.main.community.reply.v1.ReplyOuterClass
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.user.WebVideoHistoryInfo
import com.a10miaomiao.bilimiao.comm.entity.video.SubmitVideosInfo
import com.a10miaomiao.bilimiao.comm.entity.video.VideoInfo
import com.a10miaomiao.bilimiao.comm.network.ApiHelper
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class HistoryViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    var triggered = false
    var list = PaginationInfo<WebVideoHistoryInfo.ItemInfo>()

    //    private var _mapTp = 3
    private var _maxId = 0L
    private var _viewAt = 0L

    init {
        loadData(0L)
    }

    // 需更换登陆接口才能使用
    //    private fun loadData(
//        maxId: Long = _maxId
//    ) = viewModelScope.launch(Dispatchers.IO){
//        try {
//            ui.setState {
//                list.loading = true
//            }
//            val req = HistoryOuterClass.CursorV2Req.newBuilder().apply {
//                business = "archive"
//                cursor = HistoryOuterClass.Cursor.newBuilder().apply {
//                    if (maxId != 0L) {
//                        max = maxId
//                        maxTp = _mapTp // 本页最大值游标类型
//                    }
//                }.build()
//            }.build()
//            val res = HistoryGrpc.getCursorV2Method()
//                .request(req)
//                .awaitCall()
//            if (maxId == 0L){
//                list.data = mutableListOf()
//            }
//            ui.setState {
//                list.data.addAll(res.itemsList)
//                _maxId = res.cursor.max
//                _mapTp = res.cursor.maxTp
//                if (!res.hasMore) {
//                    list.finished = true
//                }
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            ui.setState {
//                list.fail = true
//            }
//        } finally {
//            ui.setState {
//                list.loading = false
//                triggered = false
//            }
//        }
//    }

    private fun loadData(
        maxId: Long = _maxId
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi.webVideoHistory(maxId, _viewAt)
                .awaitCall()
                .gson<ResultInfo<WebVideoHistoryInfo>>()
            if (res.isSuccess) {
                if (maxId == 0L) {
                    list.data = mutableListOf()
                }
                val cursor = res.data.cursor
                val listData = res.data.list
                if (listData.isNotEmpty()) {
                    ui.setState {
                        list.data.addAll(listData)
                        _maxId = cursor.max
                        _viewAt = cursor.view_at
                    }
                } else {
                    list.finished = true
                }
            } else {
                withContext(Dispatchers.Main) {
                    toast(res.message)
                }
                throw Exception(res.message)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ui.setState {
                list.fail = true
            }
        } finally {
            ui.setState {
                list.loading = false
                triggered = false
            }
        }
    }

    private fun _loadData() {
        loadData()
    }

    fun loadMode() {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(_maxId)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            _viewAt = 0
            _maxId = 0
            loadData(0L)
        }
    }
}