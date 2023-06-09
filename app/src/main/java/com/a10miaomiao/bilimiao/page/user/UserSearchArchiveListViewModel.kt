package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.archive.v1.Archive
import bilibili.app.interfaces.v1.SpaceGrpc
import bilibili.app.interfaces.v1.SpaceOuterClass
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveCursorInfo
import com.a10miaomiao.bilimiao.comm.entity.archive.ArchiveInfo
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.comm.network.request
import com.a10miaomiao.bilimiao.comm.store.UserStore
import com.a10miaomiao.bilimiao.widget.menu.CheckPopupMenu
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserSearchArchiveListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val myPage: MyPage by instance()
    val context: Context by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavArgs.id, "") }
    val name by lazy { fragment.requireArguments().getString(MainNavArgs.name) }
    var keyword = ""


    var triggered = false
    var total = 0
    var list = PaginationInfo<Archive.Arc>()

    init {
        keyword = fragment.requireArguments().getString(MainNavArgs.text, "")
        loadData(1)
    }

    private fun loadData(
        pageNum: Int = list.pageNum
    ) = viewModelScope.launch(Dispatchers.IO){
        try {
            ui.setState {
                list.loading = true
            }
            val req = SpaceOuterClass.SearchArchiveReq.newBuilder()
                .setKeyword(keyword)
                .setMid(id.toLong())
                .setPn(pageNum.toLong())
                .setPs(list.pageSize.toLong())
                .build()
            val res = SpaceGrpc.getSearchArchiveMethod()
                .request(req)
                .awaitCall()
            val archivesList = res.archivesList.map {
                it.archive
            }
            ui.setState {
                if (pageNum == 1) {
                    list.data = archivesList.toMutableList()
                } else {
                    list.data.addAll(archivesList)
                }
            }
            list.finished = archivesList.size < list.pageSize

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

    fun loadMode () {
        val (loading, finished, pageNum) = this.list
        if (!finished && !loading) {
            loadData(pageNum + 1)
        }
    }

    fun refreshList() {
        ui.setState {
            list = PaginationInfo()
            triggered = true
            loadData(1)
        }
    }

}