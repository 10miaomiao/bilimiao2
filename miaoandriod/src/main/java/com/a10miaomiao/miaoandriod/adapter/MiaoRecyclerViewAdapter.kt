package com.a10miaomiao.miaoandriod.adapter


import android.content.Context
import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.view.ViewManager
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoContextImpl
import org.jetbrains.anko.UI
import org.jetbrains.anko.matchParent

/**
 * Created by 10喵喵 on 2018/2/23.
 */
open class MiaoRecyclerViewAdapter<T>(var mRecyclerView: RecyclerView? = null) : RecyclerView.Adapter<MiaoViewHolder<T>>() {
    var itemsSource = ArrayList<T>()
        set(value) {
            if (value is MiaoList)
                value.updateView = this::notifyDataSetChanged
            field = value
        }

    lateinit var recyclerView: RecyclerView

    // private var _itemBinding: MiaoViewHolder.Binding<T>? = null

    //var viewHolder: MiaoViewHolder<T>? = null
    lateinit var _viemFn: (AnkoContext<Context>.(binding: MiaoViewHolder.Binding<T>) -> Unit)

    private val BASE_ITEM_TYPE_NORMAL = 0
    private val BASE_ITEM_TYPE_HEADER = 100000
    private val BASE_ITEM_TYPE_FOOTER = 200000
    private val BASE_ITEM_TYPE_STATE = 300000

    private val mHeaderViews = SparseArrayCompat<View>()
    private val mFootViews = SparseArrayCompat<View>()

    var onClickListener: ((item: T, position: Int) -> Unit)? = null
    var onLongClickListener: ((item: T, position: Int) -> Boolean)? = null
    var updataListener: ((holder: MiaoViewHolder<T>, item: T) -> Unit)? = null
    var onScrollStateChangedListener: ((rv: RecyclerView?, newState: Int) -> Unit)? = null
    var onScrolledListener: ((rv: RecyclerView?, dx: Int, dy: Int) -> Unit)? = null
    var onLoadMoreListener: (() -> Unit)? = null
        set(value) {
            initLoadMore()
            field = value
        }

    private var previousTotal = 0
    private var previousLoadingTime = System.currentTimeMillis()
    private var loading = true
    private var currentPage = 1

    var stateView: View? = null
        set(value) {
            field = value
            isShowStateView = true
        }
    var isShowStateView = false
        set(value) {
            field = value
            notifyItemChanged(0)
            notifyDataSetChanged()
        }

    var maxCount: Int = -1 // 最大数量，-1为无限

    override fun getItemCount() =
            if (!isShowStateView) {
                if (maxCount == -1)
                    getHeadersCount() + getFootersCount() + getRealItemCount()
                else
                    maxCount
            } else {
                1
            }

    override fun onBindViewHolder(holder: MiaoViewHolder<T>, position: Int) {
        if (isHeaderViewPos(position))
            return
        if (isFooterViewPos(position))
            return
        if (isShowStateView) {
            return
        }

        holder.index = position - getHeadersCount()
        val item = itemsSource[holder.index]
        holder.updateView(item)
        onClickListener?.let { onClick ->
            holder.parentView.setOnClickListener {
                onClick(item, holder.index)
            }
        }
        onLongClickListener?.let { onLongClick ->
            holder.parentView.setOnLongClickListener {
                onLongClick(item, holder.index)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MiaoViewHolder<T> {
        return when {
            isShowStateView -> MiaoViewHolder(stateView!!)
            mHeaderViews.get(viewType) != null -> MiaoViewHolder(mHeaderViews.get(viewType) as View)
            mFootViews.get(viewType) != null -> MiaoViewHolder(mFootViews.get(viewType) as View)
//            else -> MiaoViewHolder(parent!!.context.UI(_itemView).view)
            else -> {
                if (_viemFn == null) {
                    MiaoViewHolder(View(parent!!.context))
                } else {
                    var ankoContext = AnkoContextImpl<Context>(parent!!.context, parent!!.context, false)
                    val binding = MiaoViewHolder.Binding(this)
                    ankoContext._viemFn(binding)
                    val view = ankoContext.view
                    MiaoViewHolder(view, binding)
                }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            isShowStateView -> BASE_ITEM_TYPE_STATE
            isHeaderViewPos(position) -> mHeaderViews.keyAt(position)
            isFooterViewPos(position) -> mFootViews.keyAt(position - getHeadersCount() - getRealItemCount())
            else -> BASE_ITEM_TYPE_NORMAL
        }
    }

    fun onItemClick(listener: ((item: T, position: Int) -> Unit)): MiaoRecyclerViewAdapter<T> {
        onClickListener = listener
        return this
    }

    fun onItemLongClick(listener: ((item: T, position: Int) -> Boolean)): MiaoRecyclerViewAdapter<T> {
        onLongClickListener = listener
        return this
    }

    fun bindData(listener: (helper: MiaoViewHolder<T>, item: T) -> Unit): MiaoRecyclerViewAdapter<T> {
        updataListener = listener
        return this
    }

    fun itemView(viemFn: AnkoContext<Context>.(binding: MiaoViewHolder.Binding<T>) -> Unit): MiaoRecyclerViewAdapter<T> {
        _viemFn = viemFn
        return this
    }

    fun layoutManager(layoutManager: RecyclerView.LayoutManager): MiaoRecyclerViewAdapter<T> {
        recyclerView?.layoutManager = layoutManager
        val listData = itemsSource
        // 让RecyclerView滚动到上次位置
        if (listData is MiaoList<T>) {
            if (listData.layoutManager != null) {
                val topView = listData.layoutManager!!.getChildAt(0)
                if (topView != null) {
                    if (layoutManager is LinearLayoutManager) {
                        layoutManager.scrollToPositionWithOffset(layoutManager.getPosition(topView), topView.top)
                    }
                }
            }
            listData.layoutManager = layoutManager
        }
        return this
    }

    fun onLoadMore(listener: (() -> Unit)): MiaoRecyclerViewAdapter<T> {
        onLoadMoreListener = listener
        return this
    }

    fun addItem(item: T) {
        itemsSource.add(item)
        notifyDataSetChanged()
    }

    private fun initLoadMore() {
        val mLinearLayoutManager = mRecyclerView?.layoutManager as LinearLayoutManager
        mRecyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onScrollStateChangedListener?.invoke(recyclerView, newState)
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                onScrolledListener?.invoke(recyclerView, dx, dy)
                if (dy === 0 && mLinearLayoutManager.itemCount == 0) {
                    return
                }
                val totalItemCount = mLinearLayoutManager.itemCount
                val lastCompletelyVisiableItemPosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition()
                val now = System.currentTimeMillis()
                if (loading) {
                    if (totalItemCount > previousTotal || now - previousLoadingTime > 9999) {
                        loading = false
                    }
                }
                if (!loading && lastCompletelyVisiableItemPosition >= totalItemCount - 2) {
                    loading = true
                    previousTotal = totalItemCount
                    previousLoadingTime = now
                    onLoadMoreListener?.invoke()
                }
            }
        })
    }

    fun refresh() {
        loading = true
        previousTotal = 0
        currentPage = 1
    }

    //HeaderView 和 FootView
    fun addHeaderView(view: View): MiaoRecyclerViewAdapter<T> {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, view)
        return this
    }

    fun addHeaderView(dsl: ViewManager.() -> Unit): MiaoRecyclerViewAdapter<T> {
        mHeaderViews.put(mHeaderViews.size() + BASE_ITEM_TYPE_HEADER, recyclerView.context.UI(dsl).view)
        return this
    }

    fun addFootView(view: View): MiaoRecyclerViewAdapter<T> {
        mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, view)
        return this
    }

    fun addFootView(dsl: ViewManager.() -> Unit): MiaoRecyclerViewAdapter<T> {
        mFootViews.put(mFootViews.size() + BASE_ITEM_TYPE_FOOTER, recyclerView.context.UI(dsl).view)
        return this
    }

    private fun isHeaderViewPos(position: Int): Boolean {
        return position < getHeadersCount()
    }

    private fun isFooterViewPos(position: Int): Boolean {
        return position >= getHeadersCount() + getRealItemCount()
    }

    fun getHeadersCount(): Int = mHeaderViews.size()
    fun getFootersCount(): Int = mFootViews.size()
    fun getRealItemCount(): Int = itemsSource.size
}


