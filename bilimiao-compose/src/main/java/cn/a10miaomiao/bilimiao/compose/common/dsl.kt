package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavOptions
import cn.a10miaomiao.bilimiao.compose.R
import org.kodein.di.DI
import org.kodein.di.compose.localDI

val defaultNavOptions get() = NavOptions.Builder()
    .setEnterAnim(R.anim.miao_fragment_open_enter)
    .setExitAnim(R.anim.miao_fragment_open_exit)
    .setPopEnterAnim(R.anim.miao_fragment_close_enter)
    .setPopExitAnim(R.anim.miao_fragment_close_exit)
    .build()

@Composable
inline fun <reified VM : ViewModel> diViewModel(
    di: DI = localDI(),
    key: String? = null,
): VM {
    return diViewModel(di, key) {
        val constructor = VM::class.java.getDeclaredConstructor(
            DI::class.java
        )
        constructor.newInstance(it)
    }
}

@OptIn(ExperimentalStdlibApi::class)
@Composable
inline fun <reified VM : ViewModel> diViewModel(
    di: DI = localDI(),
    key: String? = null,
    crossinline initializer: ((di: DI) -> VM),
): VM {
    return androidx.lifecycle.viewmodel.compose.viewModel<VM>(
        key = remember(key, di) {
            val diHex = di.hashCode().toHexString()
            (key ?: VM::class.simpleName) + diHex
        },
        initializer = {
            initializer(di)
        }
    )
}