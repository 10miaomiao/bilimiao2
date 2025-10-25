package com.a10miaomiao.bilimiao.comm

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.createViewModelLazy
import androidx.lifecycle.*
import com.a10miaomiao.bilimiao.comm.store.base.BaseStore
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import org.kodein.di.*
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.type.*
import kotlin.reflect.KClass


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
    return ViewModelLazy(vmClass, { this.viewModelStore }, {
        newViewModelFactory<VM> {
            val constructor = vmClass.java.getDeclaredConstructor(
                DI::class.java
            )
            constructor.newInstance(di)
        }
    })
}

inline fun <reified VM : ViewModel> FragmentActivity.diViewModel(
    di: DI,
): Lazy<VM> = diViewModel(VM::class, di)

