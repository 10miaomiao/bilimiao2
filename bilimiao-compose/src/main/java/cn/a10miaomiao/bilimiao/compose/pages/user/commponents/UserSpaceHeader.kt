package cn.a10miaomiao.bilimiao.compose.pages.user.commponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.comm.localNavController
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@Composable
private fun UserNameBox(
    userName: String,
    sign: String,
) {
    Column {
        Text(
            text = userName,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Text(
            text = sign,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}

@Composable
private fun NumBox(
    num: String,
    title: String,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Text(text = num, color = MaterialTheme.colorScheme.onBackground)
        Text(text = title, fontSize = 12.sp, lineHeight = 12.sp, color = MaterialTheme.colorScheme.outline)
    }
}

@Composable
fun UserSpaceHeader(
    modifier: Modifier = Modifier,
    isLargeScreen: Boolean = false,
    viewModel: UserSpaceViewModel
) {
    val detailData = viewModel.detailData.collectAsState().value ?: return Box {}
    val cardData = detailData.card
    val location = cardData.space_tag?.firstOrNull {
        it.type == "location"
    }?.title ?: ""

    Box(
        modifier = modifier,
    ) {
        GlideImage(
            imageModel = UrlUtil.autoHttps(detailData.images.imgUrl),
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth()
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 80.dp)
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                GlideImage(
                    imageModel = UrlUtil.autoHttps(cardData.face) + "@200w_200h",
                    modifier = Modifier
                        .size(80.dp, 80.dp)
                        .clip(CircleShape)
                )
                if (isLargeScreen) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 45.dp, start = 10.dp), // 120 - 80
                    ) {
                        UserNameBox(
                            userName = cardData.name,
                            sign = cardData.sign,
                        )
                    }
                }
                Row(
                    Modifier
                        .padding(top = 45.dp) // 120 - 80
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumBox(
                        num = NumberUtil.converString(cardData.fans),
                        title = "粉丝",
                        onClick = viewModel::toFans,
                    )
                    VerticalDivider(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .height(20.dp))
                    NumBox(
                        num = NumberUtil.converString(cardData.attention),
                        title = "关注",
                        onClick = viewModel::toFollow,
                    )
                    VerticalDivider(
                        Modifier
                            .padding(horizontal = 20.dp)
                            .height(20.dp))
                    NumBox(
                        num = NumberUtil.converString(cardData.likes.like_num),
                        title = "获赞",
                        onClick = {

                        },
                    )
                }
            }
            if (!isLargeScreen) {
                UserNameBox(
                    userName = cardData.name,
                    sign = cardData.sign,
                )
            }
            Row(
                modifier = Modifier.padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "UID:${cardData.mid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,)

                Text(text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,)

            }
        }
    }
}