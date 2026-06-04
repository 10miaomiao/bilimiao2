package com.a10miaomiao.bilimiao.comm.platform

class JvmDeviceInfoProvider : DeviceInfoProvider {
    override val brand: String = System.getProperty("os.name") ?: "Unknown"
    override val model: String = System.getProperty("os.name") ?: "Desktop"
    override val osVersion: String = System.getProperty("os.version") ?: "0"
    override val device: String = System.getProperty("os.arch") ?: "x86_64"
    override val systemUserAgent: String
        get() {
            val osName = System.getProperty("os.name") ?: "Unknown"
            val osVersion = System.getProperty("os.version") ?: "0"
            val arch = System.getProperty("os.arch") ?: "x86_64"
            return "Mozilla/5.0 ($osName $osVersion; $arch) AppleWebKit/537.36 bilimiao-desktop"
        }
}
