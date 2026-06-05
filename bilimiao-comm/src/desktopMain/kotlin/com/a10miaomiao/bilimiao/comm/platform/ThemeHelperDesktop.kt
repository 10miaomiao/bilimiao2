package com.a10miaomiao.bilimiao.comm.platform

actual fun setDarkMode(mode: Int) {
    // Desktop: 暂不支持深色模式切换
}

actual fun getMaterialYouColor(): Int {
    // Desktop: 返回默认主题色 (粉色)
    return 0xFFFB7299.toInt()
}
