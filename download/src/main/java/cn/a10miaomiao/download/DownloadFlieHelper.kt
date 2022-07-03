package cn.a10miaomiao.download

import android.content.Context
import com.google.gson.Gson
import java.io.File

object DownloadFlieHelper {

    fun getDownloadPath(context: Context): String {
        var file = File(context.getExternalFilesDir(null), "../download")
        if (!file.exists()) {
            file.mkdir()
        }
        return file.absolutePath
    }

    fun getDownloadFileDir(context: Context, biliVideo: BiliVideoEntry): File {
        val downloadDir = File(getDownloadPath(context), biliVideo.avid.toString())
        // 创建文件夹
        if (!downloadDir.exists()) {
            downloadDir.mkdir()
        }
        var pageDir = File(downloadDir, biliVideo.page_data.page.toString())
        if (!pageDir.exists()) {
            pageDir = File(downloadDir, "c_" + biliVideo.page_data.cid.toString())
            if (!pageDir.exists()) {
                pageDir.mkdir()
            }
        }
        return pageDir
    }

    fun getEntryFile(context: Context, biliVideo: BiliVideoEntry): File {
        val pageDir = getDownloadFileDir(context, biliVideo)
        // 保存视频信息
        val entryJsonFile = File(pageDir, "entry.json")
        return entryJsonFile
    }

    fun getVideoPageFileDir(context: Context, biliVideo: BiliVideoEntry): File {
        val pageDir = getDownloadFileDir(context, biliVideo)
        val videoDir = File(pageDir, biliVideo.type_tag)
        if (!videoDir.exists()) {
            videoDir.mkdir()
        }
        return videoDir
    }

    fun getVideoPage(context: Context, biliVideo: BiliVideoEntry): BiliVideoPlayUrlEntry {
        val videoDir = getVideoPageFileDir(context, biliVideo)
        val videoJsonFile = File(videoDir, "index.json")
        val videoJsonStr = FileUtil.readTxtFile(videoJsonFile)
        val videoPage = Gson().fromJson(videoJsonStr, BiliVideoPlayUrlEntry::class.java)
        return videoPage
    }

}