package cn.a10miaomiao.bilimiao.download

import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import com.a10miaomiao.bilimiao.comm.delegate.player.entity.PlayerSourceInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService

object BiliPalyUrlHelper {
    const val DEFAULT_REFERER = "https://www.bilibili.com/"
    const val DEFAULT_USER_AGENT = "Bilibili Freedoooooom/MarkII"

    fun danmakuXMLUrl(entry: BiliDownloadEntryInfo): String {
        var cid = 0L
        val page = entry.page_data
        if (page != null) {
            cid = page.cid
        }
        val source = entry.source
        if (source != null) {
            cid = source.cid
        }
        return "https://comment.bilibili.com/$cid.xml"
    }

    fun httpHeader(entry: BiliDownloadEntryInfo): Map<String, String> {
        if (entry.page_data != null) {
            return mapOf(
                "Referer" to DEFAULT_REFERER,
                "User-Agent" to DEFAULT_USER_AGENT
            )
        }
        return mapOf(
            "User-Agent" to DEFAULT_USER_AGENT
        )
    }

    suspend fun playUrl(entry: BiliDownloadEntryInfo): BiliDownloadMediaFileInfo {
        val page = entry.page_data
        if (page != null) {
            return videoPlayUrl(entry, page)
        }
        val ep = entry.ep
        val source = entry.source
        if (ep != null && source != null) {
            return bangumiPlayUrl(entry, source, ep)
        }
        return BiliDownloadMediaFileInfo.None("")
    }

    private suspend fun videoPlayUrl(
        entry: BiliDownloadEntryInfo,
        pageData: BiliDownloadEntryInfo.PageInfo,
    ): BiliDownloadMediaFileInfo {
        val res = BiliApiService.playerAPI
            .getVideoPalyUrl(
                entry.avid!!.toString(),
                pageData.cid.toString(),
                entry.prefered_video_quality,
                if (entry.media_type == 1) {
                    1
                } else {
                    4048
                }
            )

//        it.lastPlayCid = res.last_play_cid ?: ""
//        it.lastPlayTime = res.last_play_time ?: 0
//        it.quality = res.quality
//        it.acceptList = res.accept_quality.mapIndexed { index, i ->
//            PlayerSourceInfo.AcceptInfo(i, res.accept_description[index])
//        }

        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video[0]
            val videoFile = BiliDownloadMediaFileInfo.Type2File(
                id = videoDash.id,
                base_url = videoDash.base_url,
                backup_url = videoDash.backup_url,
                bandwidth = videoDash.bandwidth,
                codecid = videoDash.codecid,
                size = 0,
                md5 = "",
                no_rexcode = false,
                frame_rate = videoDash.frame_rate,
                width = videoDash.width,
                height = videoDash.height,
                dash_drm_type = 0
            )
            var audioFileList = listOf<BiliDownloadMediaFileInfo.Type2File>()
            val audioDashList = dash.audio
            if (audioDashList?.isNotEmpty() == true) {
                val audioDash = audioDashList[0]
                audioFileList = listOf(
                    BiliDownloadMediaFileInfo.Type2File(
                        id = audioDash.id,
                        base_url = audioDash.base_url,
                        backup_url = audioDash.backup_url,
                        bandwidth = audioDash.bandwidth,
                        codecid = audioDash.codecid,
                        size = 0,
                        md5 = "",
                        no_rexcode = false,
                        frame_rate = audioDash.frame_rate,
                        width = audioDash.width,
                        height = audioDash.height,
                        dash_drm_type = 0
                    )
                )
            }
            return BiliDownloadMediaFileInfo.Type2(
                duration = dash.duration,
                video = listOf(videoFile),
                audio = audioFileList,
            )
        } else {
            val durl = res.durl!!
            val segmentList = durl.map { item ->
                BiliDownloadMediaFileInfo.Type1Segment(
                    backup_urls = listOf(),
                    bytes = item.size,
                    duration = item.length,
                    md5 = "",
                    meta_url = "",
                    order = item.order,
                    url = item.url
                )
            }
            val description = res.support_formats.find { item -> res.quality == item.quality }?.new_description ?: "清晰 480P"
            val playerCodecConfigList = listOf(
                BiliDownloadMediaFileInfo.Type1PlayerCodecConfig(
                    player = "IJK_PLAYER",
                    use_ijk_media_codec = false
                ),
                BiliDownloadMediaFileInfo.Type1PlayerCodecConfig(
                    player = "ANDROID_PLAYER",
                    use_ijk_media_codec = false
                )
            )
            return BiliDownloadMediaFileInfo.Type1(
                from = pageData.from,
                quality = entry.prefered_video_quality,
                type_tag = entry.type_tag,
                description = description,
                player_codec_config_list = playerCodecConfigList,
                user_agent = "Bilibili Freedoooooom\\/MarkII",
                segment_list = segmentList,
                parse_timestamp_milli = 0,
                available_period_milli = 0,
                is_downloaded = false,
                is_resolved = true,
                time_length = 0,
                marlin_token = "",
                video_codec_id = 0,
                video_project = true,
                format = res.format,
                player_error = 0,
                need_vip = false,
                need_login = false,
                intact = false
            )
        }

    }


    private suspend fun bangumiPlayUrl(
        entry: BiliDownloadEntryInfo,
        source: BiliDownloadEntryInfo.SourceInfo,
        ep: BiliDownloadEntryInfo.EpInfo,
    ): BiliDownloadMediaFileInfo {
        val res = BiliApiService.playerAPI
            .getBangumiUrl(
                ep.episode_id.toString(),
                source.cid.toString(),
                entry.prefered_video_quality,
                if (entry.media_type == 1) {
                    1
                } else {
                    4048
                }
            )

//        it.lastPlayCid = res.last_play_cid ?: ""
//        it.lastPlayTime = res.last_play_time ?: 0
//        it.quality = res.quality
//        it.acceptList = res.accept_quality.mapIndexed { index, i ->
//            PlayerSourceInfo.AcceptInfo(i, res.accept_description[index])
//        }

        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video[0]
            val videoFile = BiliDownloadMediaFileInfo.Type2File(
                id = videoDash.id,
                base_url = videoDash.base_url,
                backup_url = videoDash.backup_url,
                bandwidth = videoDash.bandwidth,
                codecid = videoDash.codecid,
                size = 0,
                md5 = "",
                no_rexcode = false,
                frame_rate = videoDash.frame_rate,
                width = videoDash.width,
                height = videoDash.height,
                dash_drm_type = 0
            )
            var audioFileList = listOf<BiliDownloadMediaFileInfo.Type2File>()
            val audioDashList = dash.audio
            if (audioDashList?.isNotEmpty() == true) {
                val audioDash = audioDashList[0]
                audioFileList = listOf(
                    BiliDownloadMediaFileInfo.Type2File(
                        id = audioDash.id,
                        base_url = audioDash.base_url,
                        backup_url = audioDash.backup_url,
                        bandwidth = audioDash.bandwidth,
                        codecid = audioDash.codecid,
                        size = 0,
                        md5 = "",
                        no_rexcode = false,
                        frame_rate = audioDash.frame_rate,
                        width = audioDash.width,
                        height = audioDash.height,
                        dash_drm_type = 0
                    )
                )
            }
            return BiliDownloadMediaFileInfo.Type2(
                duration = dash.duration,
                video = listOf(videoFile),
                audio = audioFileList,
            )
        } else {
            val durl = res.durl!!
            val segmentList = durl.map { item ->
                BiliDownloadMediaFileInfo.Type1Segment(
                    backup_urls = listOf(),
                    bytes = item.size,
                    duration = item.length,
                    md5 = "",
                    meta_url = "",
                    order = item.order,
                    url = item.url
                )
            }
            val description = res.support_formats.find { item -> res.quality == item.quality }?.new_description ?: "清晰 480P"
            val playerCodecConfigList = listOf(
                BiliDownloadMediaFileInfo.Type1PlayerCodecConfig(
                    player = "IJK_PLAYER",
                    use_ijk_media_codec = false
                ),
                BiliDownloadMediaFileInfo.Type1PlayerCodecConfig(
                    player = "ANDROID_PLAYER",
                    use_ijk_media_codec = false
                )
            )
            return BiliDownloadMediaFileInfo.Type1(
                from = ep.from,
                quality = entry.prefered_video_quality,
                type_tag = entry.type_tag,
                description = description,
                player_codec_config_list = playerCodecConfigList,
                user_agent = "Bilibili Freedoooooom\\/MarkII",
                segment_list = segmentList,
                parse_timestamp_milli = 0,
                available_period_milli = 0,
                is_downloaded = false,
                is_resolved = true,
                time_length = 0,
                marlin_token = "",
                video_codec_id = 0,
                video_project = true,
                format = res.format,
                player_error = 0,
                need_vip = false,
                need_login = false,
                intact = false
            )
        }

    }
}