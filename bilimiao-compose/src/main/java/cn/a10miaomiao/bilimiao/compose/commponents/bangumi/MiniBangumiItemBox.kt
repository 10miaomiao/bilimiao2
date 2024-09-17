package cn.a10miaomiao.bilimiao.compose.commponents.bangumi

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MiniBangumiItemBox(
    modifier: Modifier = Modifier,
    title: String,
    cover: String,
    desc: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier.aspectRatio(560f / 746f)
            .clip(RoundedCornerShape(5.dp))
            .clickable(onClick = onClick)
    ) {
        GlideImage(
            imageModel = UrlUtil.autoHttps(cover) + "@560w_746h",
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black),
                    )
                )
                .padding(5.dp)
                .align(Alignment.BottomStart)
        ) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
            )
            if (desc != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}