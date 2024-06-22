package cn.a10miaomiao.bilimiao.compose.comm

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.navigation.NavOptions
import cn.a10miaomiao.bilimiao.compose.R
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import org.kodein.di.DI
import org.kodein.di.android.closestDI
import org.kodein.di.bind
import org.kodein.di.bindArgSet
import org.kodein.di.bindConstant
import org.kodein.di.compose.localDI
import org.kodein.di.compose.subDI
import org.kodein.di.compose.withDI
import org.kodein.di.instance
import org.kodein.di.newInstance
import java.util.Dictionary

val defaultNavOptions get() = NavOptions.Builder()
    .setEnterAnim(R.anim.miao_fragment_open_enter)
    .setExitAnim(R.anim.miao_fragment_open_exit)
    .setPopEnterAnim(R.anim.miao_fragment_close_enter)
    .setPopExitAnim(R.anim.miao_fragment_close_exit)
    .build()

@Composable
inline fun <reified VM : ViewModel> diViewModel(): VM {
    val di = localDI()
    return androidx.lifecycle.viewmodel.compose.viewModel<VM>(
        key = di.hashCode().toString(),
        initializer = {
            val constructor = VM::class.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    )
}

@Composable
inline fun <reified VM : ViewModel> diViewModel(key: String): VM {
    val di = localDI()
    return androidx.lifecycle.viewmodel.compose.viewModel<VM>(
        key = key + di.hashCode(),
        initializer = {
            val constructor = VM::class.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    )
}
