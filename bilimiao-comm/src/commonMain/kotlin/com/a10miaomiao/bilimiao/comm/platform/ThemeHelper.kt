package com.a10miaomiao.bilimiao.comm.platform

/**
 * 设置深色模式
 * 0: 跟随系统, 1: 浅色模式, 2: 深色模式
 */
expect fun setDarkMode(mode: Int)

/**
 * 当前平台是否支持 Material You 动态主题（跟随系统壁纸/主题色取色）
 * 仅 Android 12+ 支持
 */
expect val isMaterialYouSupported: Boolean

/**
 * 获取 Material You 动态主题颜色（系统主色），用于顶部操作栏等自绘控件取色
 * 不支持的平台返回默认主题色
 */
expect fun getMaterialYouColor(): Int
