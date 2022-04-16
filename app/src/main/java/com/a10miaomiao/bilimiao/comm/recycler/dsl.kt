package com.a10miaomiao.bilimiao.comm.recycler

import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import cn.a10miaomiao.miao.binding.miaoEffect
import cn.a10miaomiao.miao.binding.miaoMemo
import cn.a10miaomiao.miao.binding.miaoRef
import com.a10miaomiao.bilimiao.comm.MiaoUI
import splitties.views.dsl.core.wrapContent
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun <T> RecyclerView._miaoAdapter(
    items: MutableList<T>? = null,
    itemUi: MiaoBindingItemUi<T>,
    depsAry: Array<*> = arrayOf<Any>(),
    isForceUpdate: Boolean = false,
    adapterInit: (MiaoBindingAdapter<T>.() -> Unit)? = null,
): MiaoBindingAdapter<T> {
    val mAdapter = miaoMemo(itemUi) {
        object : MiaoBindingAdapter<T>(
            items,
            it,
        ) {}
    }
    var isInit = false
    miaoEffect(mAdapter, {
        isInit = true
        adapterInit?.invoke(mAdapter)
        this@_miaoAdapter.adapter = mAdapter
    })
    miaoEffect(listOf(items?.size, *depsAry), {
        if (!isInit) {
            mAdapter.setList(items)
        }
    }, {
        if (isForceUpdate) {
            mAdapter.setList(items)
        }
    })
    return mAdapter
}

fun RecyclerView._miaoLayoutManage(
    lm: RecyclerView.LayoutManager
) {
    val ref = miaoRef(lm)
    val pLm = ref.value
    miaoEffect(null, {
        val topView = pLm.getChildAt(0)
        if (topView != null) {
            when (lm) {
                is LinearLayoutManager -> {
                    lm.scrollToPositionWithOffset(pLm.getPosition(topView), topView.top)
                }
                is GridLayoutManager -> {
                    lm.scrollToPositionWithOffset(pLm.getPosition(topView), topView.top)
                }
                is StaggeredGridLayoutManager -> {
                    lm.scrollToPositionWithOffset(pLm.getPosition(topView), topView.top)
                }
            }
        }
        ref.value = lm
        layoutManager = lm
    })
}

inline fun RecyclerView.headerViews(adapter: MiaoBindingAdapter<*>, block: RecyclerViews.() -> Unit) {
    RecyclerViews(
        this,
        adapter,
        0,
        MiaoUI.isRecordViews
    ).apply(block).let {
        if (MiaoUI.isRecordViews) {
            MiaoUI.parentAndViews.add(it)
        }
    }
}

inline fun RecyclerView.footerViews(adapter: MiaoBindingAdapter<*>, block: RecyclerViews.() -> Unit) {
    RecyclerViews(
        this,
        adapter,
        1,
        MiaoUI.isRecordViews
    ).apply(block).let {
        if (MiaoUI.isRecordViews) {
            MiaoUI.parentAndViews.add(it)
        }
    }
}

@OptIn(ExperimentalContracts::class)
inline fun RecyclerView.lParams(
    width: Int = wrapContent,
    height: Int = wrapContent,
    initParams: RecyclerView.LayoutParams.() -> Unit = {}
): RecyclerView.LayoutParams {
    contract { callsInPlace(initParams, InvocationKind.EXACTLY_ONCE) }
    return RecyclerView.LayoutParams(width, height).apply(initParams)
}

fun <T> Context.miaoBindingItemUi (block: MiaoBindingItemUi<T>.(item: T, index: Int) -> View): MiaoBindingItemUi<T> {
    return object : MiaoBindingItemUi<T>() {
        override val ctx: Context get() = this@miaoBindingItemUi
        override fun createView(item: T, index: Int) = block(item, index)
    }
}

fun <T> Fragment.miaoBindingItemUi(block: MiaoBindingItemUi<T>.(item: T, index: Int) -> View): MiaoBindingItemUi<T> {
    return object : MiaoBindingItemUi<T>() {
        override val ctx: Context get() = requireContext()
        override fun createView(item: T, index: Int) = block(item, index)
    }
}