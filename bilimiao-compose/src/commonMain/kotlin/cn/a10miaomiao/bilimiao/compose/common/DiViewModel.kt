package cn.a10miaomiao.bilimiao.compose.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import org.kodein.di.DI
import org.kodein.di.compose.localDI

@OptIn(ExperimentalStdlibApi::class)
@Composable
inline fun <reified VM : ViewModel> diViewModel(
    di: DI = localDI(),
    key: String? = null,
    crossinline initializer: ((di: DI) -> VM),
): VM {
    return viewModel<VM>(
        key = remember(key, di) {
            val diHex = di.hashCode().toHexString()
            (key ?: VM::class.simpleName) + diHex
        },
        initializer = {
            initializer(di)
        }
    )
}
