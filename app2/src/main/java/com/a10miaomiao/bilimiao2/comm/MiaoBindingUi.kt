package com.a10miaomiao.bilimiao2.comm

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.lifecycle.LiveData
import cn.a10miaomiao.miao.binding.MiaoBinding
import cn.a10miaomiao.miao.binding.MiaoTarget
import splitties.views.dsl.core.Ui

abstract class MiaoBindingUi() : MiaoUI() {

    private var isBatchingUpdates = false

    @PublishedApi internal val binding = MiaoBinding()

    override val root: View
        get() = binding.start<View> {
            miao { createView() }
        }

    abstract fun createView (): View

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
            binding.start {
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
            binding.start {
                createView()
            }
        }
    }



}


