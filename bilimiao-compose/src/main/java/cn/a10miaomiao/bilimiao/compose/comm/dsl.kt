package cn.a10miaomiao.bilimiao.compose.comm

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

inline fun Context.toast(msg: CharSequence) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()


fun <T : ViewModel> newViewModelFactory(initializer: (() -> T)): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <R : ViewModel> create(modelClass: Class<R>): R {
            return initializer.invoke() as R
        }
    }
}


@Suppress("MissingJvmstatic")
@Composable
public inline fun <reified VM : ViewModel> diViewModel(): VM {
    val di = localDI()
    return androidx.lifecycle.viewmodel.compose.viewModel<VM>(
        factory = newViewModelFactory<VM> {
            val constructor = VM::class.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    )

}