package com.a10miaomiao.miaoandriod

import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.os.Debug
import android.support.annotation.LayoutRes
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.UI
import org.jetbrains.anko.internals.AnkoInternals
import kotlin.properties.ObservableProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0


/**
 * Created by 10喵喵 on 2018/2/24.
 */
abstract class MiaoFragment : Fragment() {
    private var parentView: View? = null

    var binding = MiaoBindingImpl()

    // 标志位 标志已经初始化完成。
    protected var isPrepared: Boolean = false

    private var isFirstEnter = true//是否是第一次进入,默认是
    private var isReuseView = true//是否进行复用，默认复用
    private var isFragmentVisible: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val mac = render()
        val layout = layout()
        return when {
            mac != null -> {
                binding.bindFns = mac.binding.bindFns
                mac.view
            }
            layout != null -> inflater.inflate(layout, container, false)
            else -> View(context)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (parentView == null) {
            parentView = view
            initView()
            if (userVisibleHint) {
                if (isFirstEnter) {
                    onFragmentFirstVisible()
                    isFirstEnter = false
                }
                onFragmentVisibleChange(true)
                isFragmentVisible = true
            }
        }
    }

    /**
     * Fragment数据的懒加载
     */
    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (parentView == null) {
            //如果view还未初始化，不进行处理
            return
        }

        if (isFirstEnter && isVisibleToUser) {
            //如果是第一次进入并且可见
            onFragmentFirstVisible()//回调当前fragment首次可见
            isFirstEnter = false//第一次进入的标识改为false
        }
        if (isVisibleToUser) {
            //如果不是第一次进入，可见的时候
            isFragmentVisible = true
            onFragmentVisibleChange(isFragmentVisible)//回调当前fragment可见
            return
        }

        if (isFragmentVisible) {
            //如果当前fragment不可见且标识为true
            isFragmentVisible = false//更改标识
            onFragmentVisibleChange(isFragmentVisible)//回调当前fragment不可见
        }
    }

    @LayoutRes
    open fun layout(): Int? = null

    open fun render(): MiaoAnkoContext<Fragment>? = null
    open fun initView() {} //初始化组件
    open fun loadData() {}

    open fun onFragmentFirstVisible() {
        if (!isPrepared){
            isPrepared = true
            loadData()
        }
    }
    open fun onFragmentVisibleChange(isVisible: Boolean) {

    }

    protected fun isFragmentVisible(): Boolean {
        return isFragmentVisible
    }

    /**重置变量 */
    private fun resetVariavle() {
        isFirstEnter = true
        isReuseView = true
        isFragmentVisible = false
    }

    override fun onDestroy() {
        super.onDestroy()
        resetVariavle()
    }
}