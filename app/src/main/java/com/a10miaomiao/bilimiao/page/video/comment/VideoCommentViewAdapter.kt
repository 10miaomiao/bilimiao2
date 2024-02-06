package com.a10miaomiao.bilimiao.page.video.comment

import bilibili.main.community.reply.v1.ReplyOuterClass
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewContent
import com.a10miaomiao.bilimiao.commponents.comment.VideoCommentViewInfo

object VideoCommentViewAdapter {
    fun convertToVideoCommentViewInfo(
        reply: ReplyOuterClass.ReplyInfo
    ): VideoCommentViewInfo {
        return VideoCommentViewInfo(
            id = reply.id,
            oid = reply.oid,
            mid = reply.mid,
            uname = reply.member.name,
            avatar = reply.member.face,
            time = NumberUtil.converCTime(reply.ctime),
            location = reply.replyControl.location,
            floor = 0,
            content = VideoCommentViewContent(
                message = reply.content.message,
                emote = reply.content.emoteMap.values.map {
                    VideoCommentViewContent.Emote(
                        it.id, it.text, it.url
                    )
                },
                picturesList = reply.content.picturesList.map { UrlUtil.autoHttps(it.imgSrc) },
            ),
            like = reply.like,
            count = reply.count,
            cardLabels = reply.replyControl.cardLabelsList.map { it.textContent },
            isLike = reply.replyControl.action == 1L,
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