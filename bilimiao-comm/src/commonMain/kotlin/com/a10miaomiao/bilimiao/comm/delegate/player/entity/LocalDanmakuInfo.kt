package com.a10miaomiao.bilimiao.comm.delegate.player.entity

/**
 * 本地发送成功的弹幕
 *
 * [BasePlayerDelegate.sendDanmaku][com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerDelegate.sendDanmaku]
 * 发送成功后经 `localDanmakuFlow` 派发给弹幕渲染层，由渲染层创建弹幕加入引擎本地回显，
 * 并以边框区分于其它弹幕（对齐旧版 bbmiao `PlayerDelegate2.sendDanmaku`）。
 *
 * @param type 弹幕类型，取值见 [cn.a10miaomiao.bilimiao.danmaku.model.BaseDanmaku] 的 `TYPE_*`
 * @param text 弹幕文本
 * @param textSize 字体大小（B站原始字号，渲染前按屏幕密度换算）
 * @param textColor 文字颜色（0xRRGGBB）
 * @param position 弹幕出现位置（毫秒）
 */
data class LocalDanmakuInfo(
    val type: Int,
    val text: String,
    val textSize: Float,
    val textColor: Int,
    val position: Long,
)
