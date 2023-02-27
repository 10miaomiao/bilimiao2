package com.a10miaomiao.bilimiao.store

import android.app.Activity
import android.content.Context
import android.os.Debug
import android.util.DisplayMetrics
import android.view.View
import androidx.core.graphics.Insets
import androidx.lifecycle.ViewModel
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import kotlinx.coroutines.flow.MutableStateFlow
import org.kodein.di.DI


class WindowStore(
    override val di: DI
) : ViewModel(), BaseStore<WindowStore.State> {

    data class State(
        var bottomSheetFragmentID: Int = 0,
        var windowInsets: Insets = Insets(),
        var contentInsets: Insets = Insets(),
        var bottomSheetContentInsets: Insets = Insets(),
    ) {
        fun getContentInsets(view: View?): Insets {
            if (view != null && view.id == bottomSheetFragmentID) {
                return bottomSheetContentInsets
            }
            return contentInsets
        }
    }

    override val stateFlow = MutableStateFlow(State())
    override fun copyState() = state.copy()

    private var density = 1f

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

    class Insets(
        val left: Int = 0,
        val top: Int = 0,
        val right: Int = 0,
        val bottom: Int = 0,
        private val density: Float = 0f,
    ) {
        val leftDp get() = left / density
        val topDp get() = top / density
        val rightDp get() = right / density
        val bottomDp get() = bottom / density
    }

}