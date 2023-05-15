package com.a10miaomiao.bilimiao.page.video

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaListInfo
import com.a10miaomiao.bilimiao.comm.entity.media.MediaResponseInfo
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.chad.library.adapter.base.loadmore.LoadMoreStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class VideoAddFavoriteViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }

    var list = mutableListOf<MediaListInfo>()
    var loading = false
    var state = ""
    val selectedMap = HashMap<String, Boolean>()

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                state = ""
                loading = true
            }
            val res = BiliApiService.videoAPI
                .favoriteCreated(id)
                .awaitCall()
                .gson<ResultInfo<MediaResponseInfo>>()
            if (res.code == 0) {
                ui.setState {
                    list = res.data.list.toMutableList()
                }
            } else {
                withContext(Dispatchers.Main) {
                    context.toast(res.message)
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