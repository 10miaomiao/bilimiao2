package cn.a10miaomiao.bilimiao.download

import bilibili.pgc.gateway.player.v2.CodeType
import bilibili.pgc.gateway.player.v2.Stream as PlayerV2Stream
import bilibili.app.playurl.v1.Stream as PlayurlV1Stream
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadEntryInfo
import cn.a10miaomiao.bilimiao.download.entry.BiliDownloadMediaFileInfo
import com.a10miaomiao.bilimiao.comm.network.BiliApiService
import com.a10miaomiao.bilimiao.comm.network.BiliGRPCHttp
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger

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
        videoPlayUrlGrpc(entry, pageData)?.let {
            return it
        }
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
        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video.firstOrNull {
                it.id == res.quality
            } ?: dash.video.first()
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
                referer = DEFAULT_REFERER,
                user_agent = DEFAULT_USER_AGENT
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
                intact = false,
                referer = DEFAULT_REFERER,
                user_agent = DEFAULT_USER_AGENT
            )
        }
    }


    private suspend fun bangumiPlayUrl(
        entry: BiliDownloadEntryInfo,
        source: BiliDownloadEntryInfo.SourceInfo,
        ep: BiliDownloadEntryInfo.EpInfo,
    ): BiliDownloadMediaFileInfo {
        bangumiPlayUrlGrpc(entry, source, ep)?.let {
            return it
        }
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

        val dash = res.dash
        if (dash != null) {
            val videoDash = dash.video.firstOrNull {
                it.id == res.quality
            } ?: dash.video.first()
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
                user_agent = DEFAULT_USER_AGENT
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
                intact = false,
                user_agent = DEFAULT_USER_AGENT
            )
        }

    }

    private suspend fun videoPlayUrlGrpc(
        entry: BiliDownloadEntryInfo,
        pageData: BiliDownloadEntryInfo.PageInfo,
    ): BiliDownloadMediaFileInfo? {
        val quality = entry.prefered_video_quality
        val result = BiliGRPCHttp.request {
            val req = bilibili.app.playurl.v1.PlayViewReq(
                aid = entry.avid!!,
                cid = pageData.cid,
                qn = quality.toLong(),
                download = 0,
                fnval = if (entry.media_type == 1) {
                    1
                } else {
                    4048
                },
                fnver = 0,
                forceHost = 2,
                fourk = true,
            )
            bilibili.app.playurl.v1.PlayURLGRPC.playView(req)
        }.awaitCall()
        val videoInfo = result.videoInfo ?: return null
        val availableStreamList = videoInfo.streamList.filter {
            it.content != null
        }
        if (availableStreamList.isEmpty()) {
            return null
        }
        val stream = availableStreamList.firstOrNull {
            it.streamInfo?.quality == quality
        } ?: availableStreamList.firstOrNull() ?: return null
        val streamContent = stream.content ?: return null
        val streamInfo = stream.streamInfo ?: return null
        when (streamContent) {
            is PlayurlV1Stream.Content.DashVideo -> {
                val dash = streamContent.value
                val dashAudio = videoInfo.dashAudio
                val audio = dashAudio.firstOrNull {
                    it.id == dash.audioId && it.baseUrl.isNotEmpty()
                } ?: dashAudio.firstOrNull { it.baseUrl.isNotEmpty() }
                val videoFile = BiliDownloadMediaFileInfo.Type2File(
                    id = quality,
                    base_url = dash.baseUrl,
                    backup_url = dash.backupUrl,
                    bandwidth = dash.bandwidth,
                    codecid = dash.codecid,
                    size = dash.size,
                    md5 = dash.md5,
                    no_rexcode = false,
                    frame_rate = dash.frameRate,
                    width = dash.width,
                    height = dash.height,
                    dash_drm_type = 0
                )
                val audioFileList = if (audio != null) {
                    listOf(
                        BiliDownloadMediaFileInfo.Type2File(
                            id = audio.id,
                            base_url = audio.baseUrl,
                            backup_url = audio.backupUrl,
                            bandwidth = audio.bandwidth,
                            codecid = audio.codecid,
                            size = audio.size,
                            md5 = audio.md5,
                            no_rexcode = false,
                            frame_rate = audio.frameRate,
                            width = 0,
                            height = 0,
                            dash_drm_type = 0
                        )
                    )
                } else emptyList()
                return BiliDownloadMediaFileInfo.Type2(
                    duration = videoInfo.timelength / 1000,
                    video = listOf(videoFile),
                    audio = audioFileList,
                )
            }
            is PlayurlV1Stream.Content.SegmentVideo -> {
                val durl = streamContent.value
                val segmentList = durl.segment.map { item ->
                    BiliDownloadMediaFileInfo.Type1Segment(
                        backup_urls = listOf(),
                        bytes = item.size,
                        duration = item.length,
                        md5 = item.md5,
                        meta_url = "",
                        order = item.order,
                        url = item.url
                    )
                }
                val description = streamInfo.newDescription
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
                    segment_list = segmentList,
                    parse_timestamp_milli = 0,
                    available_period_milli = 0,
                    is_downloaded = false,
                    is_resolved = true,
                    time_length = 0,
                    marlin_token = "",
                    video_codec_id = 0,
                    video_project = true,
                    format = streamInfo.format,
                    player_error = 0,
                    need_vip = false,
                    need_login = false,
                    intact = false,
                )
            }
        }
    }

    private suspend fun bangumiPlayUrlGrpc(
        entry: BiliDownloadEntryInfo,
        source: BiliDownloadEntryInfo.SourceInfo,
        ep: BiliDownloadEntryInfo.EpInfo,
    ): BiliDownloadMediaFileInfo? {
        val quality = entry.prefered_video_quality
        val result = BiliGRPCHttp.request {
            val req = bilibili.pgc.gateway.player.v2.PlayViewReq(
//                seasonId = sid.toLong(),
                epid = ep.episode_id,
                cid = source.cid,
                qn = quality.toLong(),
                fnver = 0,
                fnval = if (entry.media_type == 1) {
                    1
                } else {
                    4048
                },
                fourk = true,
                forceHost = 2,
                download = 0,
                preferCodecType = CodeType.CODE264,
            )
            bilibili.pgc.gateway.player.v2.PlayURLGRPC.playView(req)
        }.awaitCall()
        val videoInfo = result.videoInfo ?: return null
        val availableStreamList = videoInfo.streamList.filter {
            it.content != null
        }
        if (availableStreamList.isEmpty()) {
            return null
        }
        val stream = availableStreamList.firstOrNull {
            it.info?.quality == quality
        } ?: availableStreamList.firstOrNull() ?: return null
        val streamContent = stream.content ?: return null
        val streamInfo = stream.info ?: return null
        when (streamContent) {
            is PlayerV2Stream.Content.DashVideo -> {
                val dash = streamContent.value
                val dashAudio = videoInfo.dashAudio
                val audio = dashAudio.firstOrNull {
                    it.id == dash.audioId && it.baseUrl.isNotEmpty()
                } ?: dashAudio.firstOrNull { it.baseUrl.isNotEmpty() }
                val videoFile = BiliDownloadMediaFileInfo.Type2File(
                    id = quality,
                    base_url = dash.baseUrl,
                    backup_url = dash.backupUrl,
                    bandwidth = dash.bandwidth,
                    codecid = dash.codecid,
                    size = dash.size,
                    md5 = dash.md5,
                    no_rexcode = false,
                    frame_rate = dash.frameRate,
                    width = dash.width,
                    height = dash.height,
                    dash_drm_type = 0
                )
                val audioFileList = if (audio != null) {
                    listOf(
                        BiliDownloadMediaFileInfo.Type2File(
                            id = audio.id,
                            base_url = audio.baseUrl,
                            backup_url = audio.backupUrl,
                            bandwidth = audio.bandwidth,
                            codecid = audio.codecid,
                            size = audio.size,
                            md5 = audio.md5,
                            no_rexcode = false,
                            frame_rate = audio.frameRate,
                            width = 0,
                            height = 0,
                            dash_drm_type = 0
                        )
                    )
                } else emptyList()
                return BiliDownloadMediaFileInfo.Type2(
                    duration = videoInfo.timelength / 1000,
                    video = listOf(videoFile),
                    audio = audioFileList,
                )
            }
            is PlayerV2Stream.Content.SegmentVideo -> {
                val durl = streamContent.value
                val segmentList = durl.segment.map { item ->
                    BiliDownloadMediaFileInfo.Type1Segment(
                        backup_urls = listOf(),
                        bytes = item.size,
                        duration = item.length,
                        md5 = item.md5,
                        meta_url = "",
                        order = item.order,
                        url = item.url
                    )
                }
                val description = streamInfo.newDescription
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
                    segment_list = segmentList,
                    parse_timestamp_milli = 0,
                    available_period_milli = 0,
                    is_downloaded = false,
                    is_resolved = true,
                    time_length = 0,
                    marlin_token = "",
                    video_codec_id = 0,
                    video_project = true,
                    format = streamInfo.format,
                    player_error = 0,
                    need_vip = false,
                    need_login = false,
                    intact = false,
                )
            }
        }
    }

}