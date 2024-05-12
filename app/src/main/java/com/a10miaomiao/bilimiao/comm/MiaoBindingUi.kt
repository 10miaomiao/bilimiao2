package com.a10miaomiao.bilimiao.comm

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.View
import cn.a10miaomiao.miao.binding.MiaoBinding
import com.a10miaomiao.bilimiao.config.config
import java.lang.Exception

abstract class MiaoBindingUi() : MiaoUI() {

    private var isBatchingUpdates = false

    @PublishedApi internal val binding = MiaoBinding()

    private var cacheView: View? = null

    private var curThemeName: String? = null

    override val root: View
        get() {
            val themeName = ctx.config.themeName
            return if (cacheView == null || themeName != curThemeName) {
                binding.start<View>(MiaoBinding.INIT) {
                    miao {
                        val view = createView()
                        curThemeName = themeName
                        cacheView = view
                        view
                    }
                }
            } else {
                binding.start(MiaoBinding.UPDATE) {
                    createView()
                }
                cacheView!!
            }
        }


    abstract fun createView (): View

    fun cleanCacheView() {
        cacheView = null
    }

    fun setState(block: () -> Unit) {
        ioHandler.post {
            block()
            if (!handler.hasMessages(0)) {
                handler.sendEmptyMessage(0)
            } else {
                isBatchingUpdates = true
            }
        }
    }

    var ioThread = HandlerThread("IO").apply {
        start()
    }

    var ioHandler = object : Handler(ioThread.looper) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            updateUi()
            if (isBatchingUpdates) {
                isBatchingUpdates = false
                updateUi()
            }
        }

        private fun updateUi() {
            binding.start(MiaoBinding.UPDATE) {
                createView()
            }
        }
    }


    var handler: Handler = object : Handler(Looper.getMainLooper()) {

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            updateUi()
        }

        private fun updateUi() {
            try {
                binding.start(MiaoBinding.UPDATE) {
                    createView()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


}


