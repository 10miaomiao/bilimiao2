package cn.a10miaomiao.bilimiao.compose.components.community

import android.os.Parcelable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Like
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Likefill
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Reply
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Share
import cn.a10miaomiao.bilimiao.compose.common.foundation.annotatedText
import cn.a10miaomiao.bilimiao.compose.common.foundation.AnnotatedTextNode
import cn.a10miaomiao.bilimiao.compose.common.foundation.ScaleIndication
import cn.a10miaomiao.bilimiao.compose.common.foundation.inlineAnnotatedContent
import cn.a10miaomiao.bilimiao.compose.components.image.ImagesGrid
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.scale.ScaleButton
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import kotlinx.android.parcel.Parcelize
import kotlin.math.max
import kotlin.math.min

@Stable
class ReplyItemBoxPictureInfo(
    val src: String,
    val width: Int,
    val height: Int,
    val size: Int,
)

@Stable
class ReplyItemBoxContentInfo(
    val message: String,
    val emote: List<EmoteInfo>,
) {
    @Stable
    class EmoteInfo(
        val id: Long,
        val text: String,
        val url: String
    )

    @Composable
    fun toAnnotatedTextNode(): List<AnnotatedTextNode> {
        val nodes = mutableListOf<AnnotatedTextNode>()
        var start = 0
        var end = 0
        while (end < message.length) {
            if (message[end] == '[') {
                val maxLen = message.length - end
                val e = emote.find {
                    val len = it.text.length
                    len <= maxLen && message.substring(end, end + len) == it.text
                }
                if (e != null) {
                    if (start < end) {
                        val nodeText = message.substring(start, end)
                        nodes.add(AnnotatedTextNode.Text(nodeText))
                    }
                    nodes.add(AnnotatedTextNode.Emote(
                        text = e.text,
                        url = UrlUtil.autoHttps(e.url)
                    ))
                    end += e.text.length
                    start = end
                    continue
                }
            }
            end++
        }
        if (start < end) {
            val nodeText = message.substring(start, end)
            nodes.add(AnnotatedTextNode.Text(nodeText))
        }
        return nodes
    }

}

@Composable
fun ReplyItemBox(
    modifier: Modifier = Modifier,
    item: bilibili.main.community.reply.v1.ReplyInfo,
    upMid: Long,
    onAvatarClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    val content = remember(item.content) {
        item.content?.let {
            ReplyItemBoxContentInfo(
                message = it.message,
                emote = it.emote.values.filterNotNull().map { emote ->
                    ReplyItemBoxContentInfo.EmoteInfo(
                        emote.id, emote.text, emote.url
                    )
                },
            )
        }
    }
    val picturesList = remember(item.content) {
        item.content?.pictures?.map {
            val imgHeight = it.imgHeight.toInt()
            val imgWidth = it.imgWidth.toInt()
            val imgSize = it.imgSize.toInt()
            val w = min(600, imgWidth)
            val h = w * imgWidth / imgHeight
            val url = UrlUtil.autoHttps(it.imgSrc)
            PreviewImageModel(
                previewUrl = url + "@${w}w_${h}h",
                originalUrl = url,
                height = imgHeight.toFloat(),
                width = imgWidth.toFloat(),
            )
        } ?: listOf()
    }
    ReplyItemBox(
        modifier = modifier,
        oid = item.oid,
        id = item.id,
        mid = item.mid,
        uname = item.member?.name ?: "",
        avatar = item.member?.face ?: "",
        time = NumberUtil.converCTime(item.ctime),
        location = item.replyControl?.location ?: "",
        floor = 0,
        content = content,
        picturesList = picturesList,
        like = item.like,
        count = item.count,
        cardLabels = item.replyControl?.cardLabels?.map { it.textContent } ?: emptyList(),
        upMid = upMid,
        isLike = item.replyControl?.action == 1L,
        onAvatarClick = onAvatarClick,
        onLikeClick = onLikeClick,
        onClick = onClick,
    )
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ReplyItemBox(
    modifier: Modifier = Modifier,
    oid: Long,
    id: Long,
    mid: Long,
    uname: String,
    avatar: String,
    time: String,
    location: String,
    floor: Int,
    content: ReplyItemBoxContentInfo?,
    picturesList: List<PreviewImageModel>,
    like: Long,
    count: Long,
    cardLabels: List<String>,
    upMid: Long,
    isLike: Boolean = false,
    onAvatarClick: () -> Unit = {},
    onLikeClick: () -> Unit = {},
    onClick: () -> Unit = {},
) {
    Row(
        Modifier
            .clickable(onClick = onClick)
            .padding(10.dp)
            .then(modifier)
    ) {
        GlideImage(
            model = UrlUtil.autoHttps(avatar) + "@200w_200h",
            loading = placeholder(R.drawable.bili_akari_img),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(40.dp)
                .clip(CircleShape)
                .clickable(onClick = onAvatarClick)
        )
        Column(
            modifier = Modifier
                .padding(start = 5.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(bottom = 2.dp)
                    .clickable(onClick = onAvatarClick),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = uname,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (upMid == mid) {
                    Text(
                        text = "UP主",
                        maxLines = 1,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                        color = Color.White,
                        fontSize = 10.sp,
                        lineHeight = 10.sp
                    )
                }
            }
            Row(
                modifier = Modifier.padding(bottom = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = time,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.outline,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
                if (floor != 0) {
                    Text(
                        text = "#${floor}",
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.outline,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                if (location.isNotBlank()) {
                    Text(
                        text = location,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.outline,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
            if (content != null) {
                val nodes = content.toAnnotatedTextNode()
                val emoteMap = inlineAnnotatedContent(nodes)
                Text(
                    annotatedText(nodes),
                    inlineContent = emoteMap,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (picturesList.isNotEmpty()) {
                Box(modifier = Modifier.padding(
                    vertical = 5.dp
                )) {
                    ImagesGrid(picturesList)
                }
            }
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(40.dp)
            ) {
                Row(
                    modifier = Modifier.clickable(
                        onClick = onLikeClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ScaleIndication,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var likeNum = like
                    if (isLike) {
                        Icon(
                            BilimiaoIcons.Common.Likefill,
                            contentDescription = "like",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                                .size(14.dp)
                        )
                        likeNum = max(1L, likeNum)
                    } else {
                        Icon(
                            BilimiaoIcons.Common.Like,
                            contentDescription = "like",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(end = 4.dp)
                                .size(14.dp)
                        )
                        likeNum = max(0L, likeNum) // 防止出现负数
                    }
                    Text(
                        text = NumberUtil.converString(likeNum),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Row(
                    modifier = Modifier.clickable(
                        onClick = { },
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ScaleIndication,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        BilimiaoIcons.Common.Reply,
                        contentDescription = "reply",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(end = 4.dp)
                            .size(14.dp)
                    )
                    Text(
                        NumberUtil.converString(count),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}