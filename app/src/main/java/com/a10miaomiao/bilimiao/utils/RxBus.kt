package com.a10miaomiao.bilimiao.utils

import com.a10miaomiao.bilimiao.entity.RxBusMsg
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor

class RxBus {
    val _bus = PublishProcessor.create<RxBusMsg>().toSerialized()

    fun send(tag: RxBusMsg) {
        _bus.onNext(tag)
    }

    fun send(tag: String) {
        send(RxBusMsg(tag))
    }

    fun send(tag: String, data: Any) {
        send(RxBusMsg(tag, data))
    }

    fun on(tag: String, fn: (data: Any?) -> Unit): Disposable {
        return toFlowable()
                .subscribe({
                    if (tag == it.tag) fn(it.data)
                },{
                    it.printStackTrace()
                })
    }

    /**
     * 根据传递的 eventType 类型返回特定事件类型的被观察者
     */
    fun toFlowable(tClass: Class<RxBusMsg>): Flowable<RxBusMsg> {
        return _bus.ofType(tClass)
    }

    fun toFlowable(): Flowable<RxBusMsg> {
        return _bus
    }

    companion object {
        private val BUS = RxBus()

        fun getInstance(): RxBus {
            return BUS
        }

        fun send(tag: RxBusMsg) {
            getInstance().send(tag)
        }

        fun send(tag: String) {
            getInstance().send(tag)
        }

        fun send(tag: String, data: Any) {
            send(RxBusMsg(tag, data))
        }

        fun on(tag: String, fn: (data: Any?) -> Unit): Disposable {
            return getInstance().on(tag, fn)
        }
    }
}