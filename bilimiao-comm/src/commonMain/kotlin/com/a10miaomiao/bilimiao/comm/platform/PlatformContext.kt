package com.a10miaomiao.bilimiao.comm.platform

import java.io.File

interface PlatformContext {
    val filesDir: File
    val cacheDir: File
    val appName: String
    val platformContext: Any
}
