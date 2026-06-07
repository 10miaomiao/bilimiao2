package cn.a10miaomiao.bilimiao.compose.common.download

import cn.a10miaomiao.bilimiao.compose.common.download.entry.BiliDownloadMediaFileInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.BasePlayerSource
import com.a10miaomiao.bilimiao.comm.delegate.player.DanmakuProvider
import com.a10miaomiao.bilimiao.comm.miao.MiaoJson
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser
import master.flame.danmaku.danmaku.parser.BiliDanmukuParser
import java.io.File

actual fun createLocalPlayerSource(
    entryDirPath: String,
    id: String,
    title: String,
    coverUrl: String,
): BasePlayerSource {
    return object : LocalPlayerSource(entryDirPath, id, title, coverUrl), DanmakuProvider {
        override suspend fun getDanmakuParser(): BaseDanmakuParser? {
            val danmakuFile = File(entryDirPath, "danmaku.xml")
            if (!danmakuFile.exists()) return null
            val inputStream = danmakuFile.inputStream()
            val loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI)
            loader.load(inputStream)
            val parser = BiliDanmukuParser()
            val dataSource = loader.dataSource
            parser.load(dataSource)
            return parser
        }
    }
}
