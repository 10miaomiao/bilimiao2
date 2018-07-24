package com.a10miaomiao.miaoandriod.adapter

import android.support.v7.widget.RecyclerView

/**
 * Created by 10喵喵 on 2018/2/24.
 */
inline fun <T> RecyclerView.miao(list: ArrayList<T>? = null, init: MiaoRecyclerViewAdapter<T>.() -> Unit): MiaoRecyclerViewAdapter<T> {
    var mAdapter = MiaoRecyclerViewAdapter<T>(this)
    mAdapter.recyclerView = this
    mAdapter.init()
    list?.let { mAdapter.itemsSource = list }
    this.adapter = mAdapter
    return mAdapter
}

inline fun <T> RecyclerView.miao(list: ArrayList<T>? = null): MiaoRecyclerViewAdapter<T> {
    var mAdapter = MiaoRecyclerViewAdapter<T>(this)
    mAdapter.recyclerView = this
    list?.let { mAdapter.itemsSource = list }
    this.adapter = mAdapter
    return mAdapter
}