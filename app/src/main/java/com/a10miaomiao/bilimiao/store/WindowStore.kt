package com.a10miaomiao.bilimiao.store

import androidx.core.graphics.Insets
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class WindowStore(override val di: DI) :
    ViewModel(), BaseStore<WindowStore.State> {

    data class State (
        var windowInsets: Insets = Insets.NONE,
        var contentInsets: Insets = Insets.NONE,
    )
    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun setWindowInsets (left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            windowInsets = Insets.of(left, top, right, bottom)
        }
    }

    fun setContentInsets (left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            windowInsets = Insets.of(left, top, right, bottom)
        }
    }

}