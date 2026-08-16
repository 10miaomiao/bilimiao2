package cn.a10miaomiao.bilimiao.compose.common.download

import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource

actual fun createLocalPlayerSource(
    entryDirPath: String,
    id: String,
    title: String,
    coverUrl: String,
): BasePlayerSource {
    return LocalPlayerSource(entryDirPath, id, title, coverUrl)
}
