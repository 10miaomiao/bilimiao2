package com.a10miaomiao.bilimiao.comm.delegate.player

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

class PlayerCoroutineScope: CoroutineScope {

    var job: Job = Job()

    // CoroutineScope 的实现
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    fun onCreate () {
        job = Job()
        job.start()
    }

    fun onDestroy () {
        job.cancel()
    }

}