package com.a10miaomiao.bilimiao.store

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class WindowStore(
    override val di: DI
) : ViewModel(), BaseStore<WindowStore.State> {

    data class State(
        var bottomAppBarHeight: Int = 0,
        var windowInsets: Insets = Insets(),
        var contentInsets: Insets = Insets(),
        var bottomSheetContentInsets: Insets = Insets(),
    ) {
        fun getContentInsets(view: View?): Insets {
            if (view != null && view.tag == "bottomSheet") {
                return bottomSheetContentInsets
            }
            return contentInsets
        }
    }

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private var density = 1f

    val bottomAppBarHeight get() = state.bottomAppBarHeight
    val bottomAppBarHeightDp get() = state.bottomAppBarHeight / density

    override fun init(context: Context) {
        super.init(context)
        density = context.resources.displayMetrics.density
    }

    fun getContentInsets(view: View?): Insets {
        return state.getContentInsets(view)
    }

    fun setWindowInsets(left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            windowInsets = Insets(left, top, right, bottom, density)
        }
    }

    fun setContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            contentInsets = Insets(left, top, right, bottom, density)
        }
    }

    fun setBottomSheetContentInsets(left: Int, top: Int, right: Int, bottom: Int) {
        this.setState {
            bottomSheetContentInsets = Insets(left, top, right, bottom, density)
        }
    }

    fun setBottomAppBarHeight(height: Int) {
        this.setState {
            bottomAppBarHeight = height
        }
    }

    data class Insets(
        val left: Int = 0,
        val top: Int = 0,
        val right: Int = 0,
        val bottom: Int = 0,
        private val density: Float = 1f,
    ) {
        val leftDp = left / density
        val topDp = top / density
        val rightDp = right / density
        val bottomDp = bottom / density
    }

}