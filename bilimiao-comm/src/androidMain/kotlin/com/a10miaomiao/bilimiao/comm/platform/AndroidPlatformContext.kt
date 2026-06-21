package com.a10miaomiao.bilimiao.comm.platform

import android.app.Application
import java.io.File

class AndroidPlatformContext(private val app: Application) : PlatformContext {
    override val filesDir: File get() = app.filesDir
    override val cacheDir: File get() = app.cacheDir
    override val appName: String = "bilimiao"
    override val platformContext: Any get() = app
}
