package com.a10miaomiao.bilimiao.comm

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelLazy
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.*
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.type.*
import kotlin.reflect.KClass

inline fun ViewGroup.views(block: MiaoUI.ViewsInfo.() -> Unit) {
    MiaoUI.ViewsInfo(this, MiaoUI.isRecordViews).apply(block).let {
        if (MiaoUI.isRecordViews) {
            MiaoUI.parentAndViews.add(it)
        }
    }
}

fun Context.miaoBindingUi (block: MiaoBindingUi.() -> View): MiaoBindingUi {
    return object : MiaoBindingUi() {
        override fun createView() = block()
        override val ctx: Context get() = this@miaoBindingUi
    }
}

fun Fragment.miaoBindingUi(block: MiaoBindingUi.() -> View): MiaoBindingUi {
    return object : MiaoBindingUi() {
        override fun createView() = block()
        override val ctx: Context get() = requireContext()
    }
}

fun Context.miaoUi (block: MiaoUI.() -> View): MiaoUI {
    return object : MiaoUI() {
        override val ctx: Context get() = this@miaoUi
        override val root = block()
    }
}


inline fun Fragment.miaoUi(noinline block: MiaoUI.() -> View): MiaoUI {
    return object : MiaoUI() {
        override val ctx: Context get() = requireContext()
        override val root = block()
    }
}

//inline fun MiaoUI.miaoCommponent(noinline block: MiaoUI.() -> View): View {
//
//}

fun <T : ViewModel> newViewModelFactory(initializer: (() -> T)): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <R : ViewModel> create(modelClass: Class<R>): R {
            return initializer.invoke() as R
        }
    }
}

fun <VM : ViewModel> Fragment.diViewModel(
    vmClass: KClass<VM>,
    di: DI,
): Lazy<VM> {
    return createViewModelLazy(vmClass, { this.viewModelStore }) {
        newViewModelFactory<VM> {
            val constructor = vmClass.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    }
}

inline fun <reified VM : ViewModel> Fragment.diViewModel(
    di: DI,
): Lazy<VM> = diViewModel(VM::class, di)

fun <VM : ViewModel> FragmentActivity.diViewModel(
    vmClass: KClass<VM>,
    di: DI,
): Lazy<VM> {
    return ViewModelLazy(vmClass, { this.viewModelStore }) {
        newViewModelFactory<VM> {
            val constructor = vmClass.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    }
}
inline fun <reified VM : ViewModel> FragmentActivity.diViewModel(
    di: DI,
): Lazy<VM> = diViewModel(VM::class, di)


fun Fragment.lazyUiDi(
    ui: () -> MiaoBindingUi,
    init: (DI.MainBuilder.() -> Unit)? = null
) = subDI(closestDI()) {
    bindSingleton { ui() }
    bindSingleton { this@lazyUiDi }
    init?.invoke(this)
}
