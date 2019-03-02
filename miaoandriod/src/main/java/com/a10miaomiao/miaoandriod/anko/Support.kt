package com.a10miaomiao.miaoandriod.anko

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.LiveData
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import com.a10miaomiao.miaoandriod.anko.MiaoAnkoContext
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.internals.AnkoInternals.createAnkoContext

inline fun <T> T.createMiaoAnkoContext(
        ctx: Context,
        init: MiaoAnkoContext<T>.() -> Unit,
        setContentView: Boolean = false
): MiaoAnkoContext<T> {
    val dsl = MiaoAnkoContext(ctx, this, setContentView)
    dsl.init()
    return dsl
}

inline fun <T : LifecycleOwner> T.createLiveAnkoContext(
        ctx: Context,
        init: LiveAnkoContext.() -> Unit,
        setContentView: Boolean = false
): LiveAnkoContext {
    val dsl = LiveAnkoContext(ctx, this, setContentView)
    dsl.init()
    return dsl
}

inline fun <T : LifecycleOwner, R> T.createLiveAnkoContext1(
        ctx: Context,
        liveData: LiveData<R>,
        init: LiveAnkoContext1<R>.() -> Unit,
        setContentView: Boolean = false
): LiveAnkoContext {
    val dsl = LiveAnkoContext1(ctx, this, setContentView, liveData)
    dsl.init()
    return dsl
}

inline fun Context.MiaoUI(setContentView: Boolean, init: MiaoAnkoContext<Context>.() -> Unit): MiaoAnkoContext<Context> =
        createMiaoAnkoContext(this, init, setContentView)

inline fun Context.MiaoUI(init: MiaoAnkoContext<Context>.() -> Unit): MiaoAnkoContext<Context> =
        createMiaoAnkoContext(this, init)

inline fun Fragment.MiaoUI(init: MiaoAnkoContext<Fragment>.() -> Unit): MiaoAnkoContext<Fragment> =
        createMiaoAnkoContext(context!!, init)

fun Fragment.liveUI(init: LiveAnkoContext.() -> Unit) = createLiveAnkoContext(activity!!, init)

fun <T> Fragment.liveUI(liveData: LiveData<T>, init: LiveAnkoContext1<T>.() -> Unit) = createLiveAnkoContext1(activity!!, liveData, init)

fun FragmentActivity.liveUI(init: LiveAnkoContext.() -> Unit) = createLiveAnkoContext(this, init)

fun <T> FragmentActivity.liveUI(liveData: LiveData<T>, init: LiveAnkoContext1<T>.() -> Unit) = createLiveAnkoContext1(this, liveData, init)