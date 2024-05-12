package cn.a10miaomiao.bilimiao.compose.commponents.video

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun VideoItem(
    modifier: Modifier = Modifier,
    title: String? = null,
    pic: String? = null,
    upperName: String? = null,
    remark: String? = null,
    playNum: String? = null,
    damukuNum: String? = null,
    duration: String? = null,
    isHtml: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        if (pic != null) {
            GlideImage(
                imageModel = UrlUtil.autoHttps(pic) + "@672w_378h_1c_",
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(5.dp))
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .height(80.dp)
                .padding(horizontal = 10.dp),
        ) {
            if (title != null) {
                Text(
                    text = title,
                    maxLines = 2,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                )
            }



//            Text(
//                text = "${it.media_count}个视频",
//                maxLines = 1,
//                color = MaterialTheme.colorScheme.outline,
//                overflow = TextOverflow.Ellipsis,
//            )
        }

    }
}