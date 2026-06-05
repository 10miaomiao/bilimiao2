package com.a10miaomiao.bilimiao.comm.platform

/**
 * 设置深色模式
 * 0: 跟随系统, 1: 浅色模式, 2: 深色模式
 */
expect fun setDarkMode(mode: Int)

/**
 * 获取 Material You 动态主题颜色
 * 仅 Android 12+ 支持，其他平台返回默认颜色
 */
expect fun getMaterialYouColor(): Int
