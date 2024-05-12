package com.a10miaomiao.bilimiao.page.user.archive

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bilibili.app.interfaces.v1.SearchArchiveReq
import bilibili.app.interfaces.v1.SpaceGRPC
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.entity.comm.PaginationInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

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
    var list = PaginationInfo<bilibili.app.archive.v1.Arc>()

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
            val req = SearchArchiveReq(
                keyword = keyword,
                mid = id.toLong(),
                pn = pageNum.toLong(),
                ps = list.pageSize.toLong(),
            )
            val res = BiliGRPCHttp.request {
                SpaceGRPC.searchArchive(req)
            }.awaitCall()
            val archivesList = res.archives.map {
                it.archive
            }.filterNotNull()
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