package com.a10miaomiao.bilimiao.page.download

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.a10miaomiao.download.BiliVideoEntry
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.delegate.download.DownloadDelegate
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.DownloadStore
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

@OptIn(InternalCoroutinesApi::class)
class DownloadViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val downloadStore by instance<DownloadStore>()
    val downloadDelegate by instance<DownloadDelegate>()

    init {
//        viewModelScope.launch {
////            downloadStore.connectUi(ui)
//            downloadStore.stateFlow.collect(object : FlowCollector<DownloadStore.State> {
//                override suspend fun emit(value: DownloadStore.State) {
//                    DebugMiao.log("DownloadInfo-1", value.curDownload)
//                    ui.setState {  }
//                }
//            })
//        }
    }

    fun delectDownload (item: BiliVideoEntry) {
        ui.setState {
            downloadDelegate.downloadService.delectDownload(item)
        }
    }
}