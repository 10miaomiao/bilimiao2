package com.a10miaomiao.bilimiao.page.filter

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.FilterStore
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class FilterWordListViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val context: Context by instance()
    val ui: MiaoBindingUi by instance()

    val filterStore by instance<FilterStore>()

    val list get() = filterStore.state.filterWordList
    val count get() = filterStore.filterWordCount
    val selectedList = HashSet<String>()
    val isSelectAll get() = filterStore.filterWordCount == selectedList.size


    init {
        viewModelScope.launch {
            filterStore.connectUi(ui)
        }
    }


    fun selectedChange(value: String, isChecked: Boolean) {
        if (isChecked) {
            selectedList.add(value)
        } else {
            selectedList.remove(value)
        }
    }

    fun deleteSelected() {
        val keywordList = selectedList.toList()
        if (keywordList.isEmpty()) {
            context.toast("未选择关键字")
        }
        filterStore.deleteWord(keywordList)
        unSelectAll()
    }

    fun selectAll() {
        ui.setState {
            selectedList.addAll(list)
        }
    }

    fun unSelectAll() {
        ui.setState {
            selectedList.clear()
        }
    }

}