package com.a10miaomiao.bilimiao.comm.recycler

import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.loadmore.BaseLoadMoreView
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class CustomLoadMoreView : BaseLoadMoreView() {

    override fun getRootView(parent: ViewGroup): View {
        return View(parent.context)
    }

    override fun getLoadComplete(holder: BaseViewHolder): View {
        return holder.itemView
    }

    override fun getLoadEndView(holder: BaseViewHolder): View {
        return holder.itemView
    }

    override fun getLoadFailView(holder: BaseViewHolder): View {
        return holder.itemView
    }

    override fun getLoadingView(holder: BaseViewHolder): View {
        return holder.itemView
    }
}