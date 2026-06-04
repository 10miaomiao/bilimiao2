package com.a10miaomiao.bilimiao.comm.platform

import java.io.File

class JvmPlatformContext : PlatformContext {
    override val filesDir: File = File(System.getProperty("user.home"), ".bilimiao").also { it.mkdirs() }
    override val appName: String = "bilimiao"
    override val platformContext: Any get() = this
}
