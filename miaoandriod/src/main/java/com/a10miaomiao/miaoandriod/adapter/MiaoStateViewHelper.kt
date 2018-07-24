package com.a10miaomiao.miaoandriod.adapter

import android.view.View

class MiaoStateViewHelper<T>(var mAdapter: MiaoRecyclerViewAdapter<T>, var view: View) {

    /**
     * 显示
     */
    fun show(){
        mAdapter.stateView = view
    }

    /**
     * 隐藏
     */
    fun hide(){
        mAdapter.isShowStateView = false
    }

    fun isShow(): Boolean{
        return mAdapter.isShowStateView
    }

}