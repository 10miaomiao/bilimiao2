package com.a10miaomiao.bilimiao.comm.delegate.player

/**
 * 本地弹幕数据源。
 *
 * 用于让播放器代理（Android `PlayerDelegate2` / 桌面端 `DesktopPlayerDelegate`）
 * 在播放本地下载文件时，从 [BasePlayerSource] 读取已缓存的 B 站弹幕 XML 原始字节，
 * 而不必走网络 `playerAPI.getDanmakuList`。
 *
 * - Android：由 `LocalPlayerSourceFactoryAndroid` 同时实现 [DanmakuProvider]（DFM 解析器），
 *   现有路径不受影响。本接口作为可选补充，未来可统一为直接返回字节。
 * - 桌面端：由 `LocalPlayerSourceFactoryDesktop` 实现本接口，
 *   `DesktopPlayerDelegate` 检查 `source is LocalDanmakuSource` 后用 danmaku-engine
 *   的 `BiliDanmakuParser` 解析。
 *
 * 返回 `null` 表示该源没有本地弹幕（例如尚未下载完成或文件损坏），
 * 调用方应回退到网络弹幕接口。
 */
interface LocalDanmakuSource {
    /**
     * 读取 B 站弹幕 XML 的原始（已解压）字节。
     *
     * 对应下载目录下 `entryDir/danmaku.xml` 的内容。
     * 不做任何解析，仅负责读取字节；解析由平台各自的弹幕引擎完成。
     */
    suspend fun getLocalDanmakuXmlBytes(): ByteArray?
}
