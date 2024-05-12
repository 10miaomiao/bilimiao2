package com.a10miaomiao.bilimiao.page.video.comment

import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewContent
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewInfo

object VideoCommentViewAdapter {

    fun defaultVideoCommentViewInfo(): VideoCommentViewInfo {
        return VideoCommentViewInfo(
            id = 0L,
            oid = 0L,
            mid = 0L,
            uname = "",
            avatar = "",
            time = "",
            location = "",
            floor = 0,
            content = VideoCommentViewContent(
                message = "",
                emote = emptyList(),
                picturesList = emptyList(),
            ),
            like = 0,
            count = 0,
            cardLabels = emptyList(),
            isLike = false,
        )
    }

    fun convertToVideoCommentViewInfo(
        reply: bilibili.main.community.reply.v1.ReplyInfo,
    ): VideoCommentViewInfo {
        return VideoCommentViewInfo(
            id = reply.id,
            oid = reply.oid,
            mid = reply.mid,
            uname = reply.member?.name ?: "",
            avatar = reply.member?.face ?: "",
            time = NumberUtil.converCTime(reply.ctime),
            location = reply.replyControl?.location ?: "",
            floor = 0,
            content = reply.content?.let {
                VideoCommentViewContent(
                    message = it.message,
                    emote = it.emote.values.filterNotNull().map { emote ->
                        VideoCommentViewContent.Emote(
                            emote.id, emote.text, emote.url
                        )
                    },
                    picturesList = it.pictures.map { UrlUtil.autoHttps(it.imgSrc) },
                )
            } ?: VideoCommentViewContent(
                message = "",
                emote = null,
                picturesList = emptyList(),
            ),
            like = reply.like,
            count = reply.count,
            cardLabels = reply.replyControl?.cardLabels?.map { it.textContent } ?: emptyList(),
            isLike = reply.replyControl?.action == 1L,
        )
    }

    fun convertToVideoCommentViewInfo(
        reply: VideoCommentReplyInfo
    ): VideoCommentViewInfo {
        return VideoCommentViewInfo(
            id = reply.rpid,
            oid = reply.oid,
            mid = reply.mid,
            uname = reply.member.uname,
            avatar = reply.member.avatar,
            time = NumberUtil.converCTime(reply.ctime),
            location = reply.reply_control.location ?: "",
            floor = reply.floor,
            content = VideoCommentViewContent(
                message = reply.content.message,
                emote = reply.content.emote?.values?.map {
                    VideoCommentViewContent.Emote(
                        it.id, it.text, it.url
                    )
                } ?: emptyList(),
                picturesList = emptyList(),
            ),
            like = reply.like,
            count = reply.count,
            cardLabels = listOf(),
            isLike = false,
        )
    }
}