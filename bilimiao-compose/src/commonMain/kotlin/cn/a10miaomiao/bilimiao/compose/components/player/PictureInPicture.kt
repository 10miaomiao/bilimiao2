package cn.a10miaomiao.bilimiao.compose.components.player

/**
 * 是否支持画中画（Picture-in-Picture）
 *
 * - 安卓：Android 8.0（API 26）及以上支持
 * - 桌面：暂不支持
 */
expect fun isPictureInPictureSupported(): Boolean

/**
 * 进入画中画模式
 *
 * @param aspectWidth 视频画面宽度（仅用于计算宽高比）
 * @param aspectHeight 视频画面高度
 * @return 是否成功进入画中画
 */
expect fun enterPictureInPictureMode(aspectWidth: Int, aspectHeight: Int): Boolean
