package com.a10miaomiao.bilimiao.store

import android.view.View
import androidx.core.graphics.Insets
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class WindowStore(override val di: DI) :
    ViewModel(), BaseStore<WindowStore.State> {

    data class State (
        var windowInsets: Insets = Insets.NONE,
        var contentInsets: Insets = Insets.NONE,
        var bottomSheetContentInsets: Insets = Insets.NONE,
    )
    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    fun getContentInsets(view: View?): Insets {
        if (view != null && view.id == R.id.nav_bottom_sheet_fragment) {
            return state.bottomSheetContentInsets
        }
        return state.contentInsets
    }

    fun setWindowInsets (left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            windowInsets = Insets.of(left, top, right, bottom)
        }
    }

    fun setContentInsets (left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            contentInsets = Insets.of(left, top, right, bottom)
        }
    }

    fun setBottomSheetContentInsets (left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            bottomSheetContentInsets = Insets.of(left, top, right, bottom)
        }
    }

}