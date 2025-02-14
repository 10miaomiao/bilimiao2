package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.R
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun VideoCoverBox(
    modifier: Modifier = Modifier,
    aid: Long ,
    pic: String,
    title: String,
    duration: Long,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .then(modifier)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        GlideImage(
            model = UrlUtil.autoHttps(pic),
            contentDescription = title,
            modifier = Modifier.fillMaxSize(),
            loading = placeholder(R.drawable.bili_default_placeholder_img_tv),
            failure = placeholder(R.drawable.bili_fail_placeholder_img_tv),
            contentScale = ContentScale.Crop,
        )
        Column() {
            Text(title)
        }
    }
}