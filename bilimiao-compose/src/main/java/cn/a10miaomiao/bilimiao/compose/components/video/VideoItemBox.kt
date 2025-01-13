package cn.a10miaomiao.bilimiao.compose.components.video

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.semantics.textSubstitution
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.assets.BilimiaoIcons
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.Common
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Danmukunum
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Playnum
import cn.a10miaomiao.bilimiao.compose.assets.bilimiaoicons.common.Upper
import cn.a10miaomiao.bilimiao.compose.common.foundation.htmlText
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoItemBox(
    modifier: Modifier = Modifier,
    title: String? = null,
    pic: String? = null,
    upperName: String? = null,
    remark: String? = null,
    playNum: String? = null,
    damukuNum: String? = null,
    duration: String? = null,
    progress: Float = -1f,
    isHtml: Boolean = false,
    onClick: (() -> Unit)? = null,
) {

    Row(
        modifier = Modifier
            .run {
                if (onClick == null) this
                else clickable(onClick = onClick)
            }
            .then(modifier)
    ) {
        if (pic != null) {
            Box(
                modifier = Modifier
                    .size(width = 140.dp, height = 85.dp)
                    .clip(RoundedCornerShape(5.dp)),
            ) {
                GlideImage(
                    model = UrlUtil.autoHttps(pic) + "@672w_378h_1c_",
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
                    failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
                )
                if (duration != null) {
                    Box(
                        modifier = Modifier
                            .wrapContentHeight()
                            .align(Alignment.BottomEnd)
                            .padding(5.dp)
                            .background(
                                color = Color(0x99000000),
                                shape = RoundedCornerShape(5.dp)
                            )
                            .padding(vertical = 2.dp, horizontal = 4.dp),
                    ) {
                        Text(
                            text = duration,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.semantics {
                                contentDescription = "视频时长：$duration"
                            }
                        )
                    }
                }
                if (progress > 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                            .align(Alignment.BottomStart),
                        drawStopIndicator = { }
                    )
                }
            }

        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(85.dp)
                .padding(start = 5.dp)
                .zIndex(-1f), // 适配无障碍功能，优先播报视频标题
        ) {
            if (title != null) {
                if (isHtml) {
                    Text(
                        text = htmlText(title),
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                    )
                } else {
                    Text(
                        text = title,
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }

            if (upperName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = BilimiaoIcons.Common.Upper,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 2.dp)
                            .semantics {
                                contentDescription = "up主：$upperName"
                            },
                        text = upperName,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            if (remark != null) {
                Text(
                    modifier = Modifier.padding(top = 2.dp),
                    text = NumberUtil.converString(remark),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (playNum != null && damukuNum != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = BilimiaoIcons.Common.Playnum,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 2.dp)
                            .semantics {
                                contentDescription = "播放量：$playNum"
                            },
                        text = NumberUtil.converString(playNum),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        imageVector = BilimiaoIcons.Common.Danmukunum,
                        contentDescription = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 2.dp)
                            .semantics {
                                contentDescription = "弹幕数：$damukuNum"
                            },
                        text = NumberUtil.converString(damukuNum),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

    }
}


@Preview
@Composable
fun VideoItemBoxPreview() {
    VideoItemBox(
        modifier = Modifier.width(400.dp),
        title = "【原神拜年纪】让风告诉你",
        upperName = "原神",
        pic = "http://i0.hdslb.com/bfs/archive/9b920aa311b26f2d17ef7aece043cdcdbca6b27b.jpg",
        playNum = "1234523",
        damukuNum = "234234",
        onClick = {

        }
    )
}