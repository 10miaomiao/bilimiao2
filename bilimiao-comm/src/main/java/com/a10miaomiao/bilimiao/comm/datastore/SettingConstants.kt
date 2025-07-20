package com.a10miaomiao.bilimiao.comm.datastore

object SettingConstants {

    const val FLAGS_SUB_CONTENT_SHOW = "flags_sub_content_show"

    const val HOME_ENTRY_VIEW_DEFAULT = 0 // 时光姬
    const val HOME_ENTRY_VIEW_RECOMMEND = 1 // 推荐
    const val HOME_ENTRY_VIEW_POPULAR = 2 // 热门

    const val PLAYER_DECODER_DEFAULT = "default"
    const val PLAYER_DECODER_AV1 = "AV1"

    const val PLAYER_FNVAL_FLV = 2
    const val PLAYER_FNVAL_MP4 = 2
    const val PLAYER_FNVAL_DASH = 4048

    // 0000 0000：什么都不做
    const val PLAYER_OPEN_MODE_DEFAULT = 0
    // 0000 0001：无播放时，自动播放
    const val PLAYER_OPEN_MODE_AUTO_PLAY = 1
    // 0000 0010：自动替换播放中的视频
    const val PLAYER_OPEN_MODE_AUTO_REPLACE = 2
    // 0000 0100：自动替换暂停暂停的视频
    const val PLAYER_OPEN_MODE_AUTO_REPLACE_PAUSE = 4
    // 0000 1000：自动替换播放完成的视频
    const val PLAYER_OPEN_MODE_AUTO_REPLACE_COMPLETE = 8
    // 0001 0000：自动关闭
    const val PLAYER_OPEN_MODE_AUTO_CLOSE = 16
    // 0010 0000：竖屏状态自动全屏
    const val PLAYER_OPEN_MODE_AUTO_FULL_SCREEN = 32
    // 0100 0000：横屏状态自动横屏
    const val PLAYER_OPEN_MODE_AUTO_FULL_SCREEN_LANDSCAPE = 64

    // 0000：播放完结束
    const val PLAYER_ORDER_END = 0
    // 0001：播放完循环
    const val PLAYER_ORDER_LOOP = 1
    // 0010：自动下一P
    const val PLAYER_ORDER_NEXT_P = 2
    // 0100：自动下一个视频
    const val PLAYER_ORDER_NEXT_VIDEO = 4
    // 1000：自动下一集（番剧）
    const val PLAYER_ORDER_NEXT_EPISODE = 8
    // 默认：自动下一P + 自动下一个视频 + 自动下一集（番剧）
    const val PLAYER_ORDER_DEFAULT = PLAYER_ORDER_NEXT_P or PLAYER_ORDER_NEXT_VIDEO or PLAYER_ORDER_NEXT_EPISODE

    // 跟随视频
    const val PLAYER_FULL_MODE_AUTO = 0
    // 跟随系统
    const val PLAYER_FULL_MODE_UNSPECIFIED = 8
    // 横向全屏(自动旋转)
    const val PLAYER_FULL_MODE_SENSOR_LANDSCAPE = 3
    // 横向全屏(固定方向1)
    const val PLAYER_FULL_MODE_LANDSCAPE = 1
    // 横向全屏(固定方向2)
    const val PLAYER_FULL_MODE_REVERSE_LANDSCAPE = 2

    // 小屏时显示底部进度条
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_SMALL = 1
    // 全屏时显示底部进度条
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_FULL = 2
    // 画中画时显示底部进度条
    const val  PLAYER_BOTTOM_PROGRESS_BAR_SHOW_IN_PIP = 4

    // 倍速值集合
    val PLAYER_SPEED_SETS = setOf("0.5", "1.0", "2.0")

}