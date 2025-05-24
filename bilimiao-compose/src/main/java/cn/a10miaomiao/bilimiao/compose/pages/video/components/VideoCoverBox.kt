package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalFoundationApi::class)
@Composable
fun VideoCoverBox(
    modifier: Modifier = Modifier,
    aid: Long ,
    pic: String,
    title: String,
    progressTitle: String,
    progress: Long,
    duration: Long,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .clip(RoundedCornerShape(8.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            )
    ) {
        GlideImage(
            model = UrlUtil.autoHttps(pic),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
            failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    )
                )
                .padding(5.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            if (progress != 0L && progressTitle.isNotBlank()) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "上次看到：${progressTitle}",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text(
                    modifier = Modifier.weight(1f),
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            if (progress == 0L) {
                val durationText = NumberUtil.converDuration(duration)
                Text(
                    modifier = Modifier
                        .semantics {
                            contentDescription = "视频时长：$durationText"
                        },
                    text = durationText,
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                val durationText = NumberUtil.converDuration(duration)
                val progressText = NumberUtil.converDuration(progress)
                Text(
                    modifier = Modifier
                        .semantics {
                            contentDescription = "视频时长：$durationText，观看进度：$progressText"
                        },
                    text = "$progressText/$durationText",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.ic_bili_tv_play),
            contentDescription = "立即播放",
            modifier = Modifier
                .align(Alignment.Center)
                .width(50.dp),
            contentScale = ContentScale.FillWidth,
        )
    }
}
