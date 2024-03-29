package com.a10miaomiao.bilimiao.page.user.archive

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveSeasonVideosInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaDetailInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListV2Info
import com.a10miaomiao.bilimiao.comm.entity.media.MediaResponseV2Info
import com.a10miaomiao.bilimiao.comm.entity.media.MediasInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserSeriesDetailViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    val type by lazy { fragment.requireArguments().getString(MainNavArgs.type, "") }
    val name by lazy { fragment.requireArguments().getString(MainNavArgs.name, "") }

    var triggered = false
    var list = PaginationInfo<MediaListV2Info>()
    private var _oid: String = ""

    init {
        loadData("")
    }

    private fun loadData(
        oid: String,
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            _oid = oid
            ui.setState {
                list.loading = true
            }
            val res = BiliApiService.userApi
                .medialistResourceList(
                    bizId = id,
                    type = type,
                    oid = oid,
                )
                .awaitCall()
                .gson<ResultInfo<MediaResponseV2Info>>()
            if (res.code == 0) {
                val mediaList = res.data.media_list
                if (mediaList != null) {
                    ui.setState {
                        if (oid.isBlank()) {
                            list.data = mediaList.toMutableList()
                        } else {
                            list.data.addAll(mediaList.filter { i1 ->
                                list.data.indexOfFirst { i2 -> i1.id == i2.id } == -1
                            })
                        }
                    }
                }
                list.finished = !res.data.has_more
            } else {
                withContext(Dispatchers.Main) {
                    PopTip.show(res.message)
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
        loadData(_oid)
    }

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(
                list.data.lastOrNull()?.id ?: ""
            )
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData("")
        }
    }
}