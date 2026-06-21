package com.a10miaomiao.bilimiao.comm.platform

import com.a10miaomiao.bilimiao.comm.platform.storage.AppFolderResolver
import com.a10miaomiao.bilimiao.comm.platform.storage.AppInfo
import java.io.File

class JvmPlatformContext : PlatformContext {
    private val appDataDirectories = AppFolderResolver.INSTANCE.resolve(
        AppInfo(
            qualifier = "cn.10miaomiao",
            organization = "10miaomiao",
            name = "bilimiao",
        )
    )

    override val filesDir: File = appDataDirectories.data
    override val cacheDir: File = appDataDirectories.cache
    override val appName: String = "bilimiao"
    override val platformContext: Any get() = this
}
