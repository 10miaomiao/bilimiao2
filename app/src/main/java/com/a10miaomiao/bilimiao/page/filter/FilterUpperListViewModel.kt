package com.a10miaomiao.bilimiao.page.filter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.connectUi
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class FilterUpperListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    val filterStore by instance<FilterStore>()

    val list get() = filterStore.state.filterUpperList
    val count get() = filterStore.filterUpperCount
    val selectedList = HashSet<Long>()
    val isSelectAll get() = filterStore.filterUpperCount == selectedList.size


    init {
        viewModelScope.launch {
            filterStore.connectUi(ui)
        }
    }


    fun selectedChange(mid: Long, isChecked: Boolean) {
        if (isChecked) {
            selectedList.add(mid)
        } else {
            selectedList.remove(mid)
        }
    }

    fun deleteSelected() {
        val upperList = selectedList.toList()
        filterStore.deleteUpper(upperList)
        unSelectAll()
    }

    fun selectAll() {
        ui.setState {
            selectedList.addAll(list.map { it.mid })
        }
    }

    fun unSelectAll() {
        ui.setState {
            selectedList.clear()
        }
    }
}