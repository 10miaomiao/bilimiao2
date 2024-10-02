package cn.a10miaomiao.bilimiao.compose.pages.user.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.pages.user.UserArchiveViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.skydoves.landscapist.glide.GlideImage

@Composable
private fun UserNameBox(
    userName: String,
    sign: String,
    level: Int,
    officialVerify: Boolean,
    officialVerifyTitle: String,
    officialVerifyIcon: String,
) {
    val levelImgRes = when (level) {
        0 -> R.drawable.ic_bili_lv0
        1 -> R.drawable.ic_bili_lv1
        2 -> R.drawable.ic_bili_lv2
        3 -> R.drawable.ic_bili_lv3
        4 -> R.drawable.ic_bili_lv4
        5 -> R.drawable.ic_bili_lv5
        6 -> R.drawable.ic_bili_lv6
        7 -> R.drawable.ic_bili_lv7
        8 -> R.drawable.ic_bili_lv8
        else -> R.drawable.ic_bili_lv9
    }
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Image(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(24.dp, 18.dp),
                painter = painterResource(levelImgRes),
                contentDescription = "lv${level}"
            )
        }
        if (officialVerify) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlideImage(
                    imageModel = UrlUtil.autoHttps(officialVerifyIcon),
                    modifier = Modifier
                        .padding(end = 2.dp)
                        .size(16.dp),
                )
                Text(
                    officialVerifyTitle,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
        Text(
            text = sign,
            style = MaterialTheme.typography.labelLarge,
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
        modifier = Modifier
            .clickable(onClick = onClick)
            .widthIn(min = 60.dp)
            .padding(horizontal = 4.dp),
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
    viewModel: UserSpaceViewModel,
    archiveViewModel: UserArchiveViewModel,
) {
    val detailData = viewModel.detailData.collectAsState().value ?: return Box {}
    val cardData = detailData.card
    val location = cardData.space_tag?.firstOrNull {
        it.type == "location"
    }?.title ?: ""
    val officialVerify = cardData.official_verify

    val seriesList = archiveViewModel.seriesList.collectAsState().value
    val seriesTotal = archiveViewModel.seriesTotal.collectAsState().value

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
                .padding(top = 80.dp, start = 10.dp)
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
                            level = cardData.level_info.current_level,
                            officialVerify = officialVerify.title.isNotBlank(),
                            officialVerifyTitle = officialVerify.title,
                            officialVerifyIcon = officialVerify.icon,
                        )
                    }
                }
                Row(
                    Modifier.padding(top = 45.dp), // 120 - 80
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    NumBox(
                        num = NumberUtil.converString(cardData.fans),
                        title = "粉丝",
                        onClick = viewModel::toFans,
                    )
                    VerticalDivider(Modifier.height(20.dp))
                    NumBox(
                        num = NumberUtil.converString(cardData.attention),
                        title = "关注",
                        onClick = viewModel::toFollow,
                    )
                    VerticalDivider(Modifier.height(20.dp))
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
                    level = cardData.level_info.current_level,
                    officialVerify = officialVerify.title.isNotBlank(),
                    officialVerifyTitle = officialVerify.title,
                    officialVerifyIcon = officialVerify.icon,
                )
            }
            Row(
                modifier = Modifier.padding(top = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "UID:${cardData.mid}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

                Text(
                    text = location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )

            }

            val seriesHeight = 36.dp
            if (seriesList.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .fillMaxWidth()
                        .height(seriesHeight)
                        .clip(RoundedCornerShape(4.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    contentPadding = PaddingValues(end = 10.dp),
                ) {
                    items(seriesList, { it.param }) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .height(seriesHeight)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // collections
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.List,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 5.dp)
                                    .size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = it.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (seriesTotal > seriesList.size) {
                        item {
                            Row(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .height(seriesHeight)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "更多合集",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}