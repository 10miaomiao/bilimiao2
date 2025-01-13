package cn.a10miaomiao.bilimiao.compose.pages.user.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cn.a10miaomiao.bilimiao.compose.R
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.provider.localImagePreviewerController
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.ModelProcessor
import cn.a10miaomiao.bilimiao.compose.components.user.UserLevelIcon
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState
import cn.a10miaomiao.bilimiao.compose.pages.user.UserArchiveViewModel
import cn.a10miaomiao.bilimiao.compose.pages.user.UserSpaceViewModel
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.utils.UrlUtil
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserFaceImage(
    face: String,
) {
    val previewerController = localImagePreviewerController()
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { 1 },
        getKey = { face },
    )
    val itemState = rememberTransformItemState(
        intrinsicSize = Size(200f, 200f),
    )
    Box(
        modifier = Modifier.size(80.dp, 80.dp)
            .clip(CircleShape)
            .clickable {
                previewerController.enterTransform(
                    previewerState,
                    listOf(
                        PreviewImageModel(
                            originalUrl = UrlUtil.autoHttps(face),
                            previewUrl = UrlUtil.autoHttps(face) + "@200w_200h",
                            height = 200f,
                            width = 200f
                        )
                    ),
                )
            },
    ) {
        TransformItemView(
            key = face,
            itemState = itemState,
            transformState = previewerState,
        ) {
            GlideImage(
                modifier = Modifier.fillMaxSize()
                    .clip(CircleShape),
                model = UrlUtil.autoHttps(face) + "@200w_200h",
                contentDescription = null,
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun UserNameBox(
    userName: String,
    sign: String,
    level: Int,
    officialVerify: Boolean,
    officialVerifyTitle: String,
    officialVerifyIcon: String,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            UserLevelIcon(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .size(24.dp, 18.dp),
                level = level,
            )
        }
        if (officialVerify) {
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlideImage(
                    model = UrlUtil.autoHttps(officialVerifyIcon),
                    contentDescription = null,
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

@OptIn(ExperimentalGlideComposeApi::class)
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
            model = UrlUtil.autoHttps(detailData.images.imgUrl),
            contentDescription = null,
            contentScale = ContentScale.Crop,
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
                UserFaceImage(cardData.face)
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
                        onClick = viewModel::showLikeInfo,
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
                                .clip(RoundedCornerShape(4.dp))
                                .clickable {
                                    archiveViewModel.toSeriesDetail(it)
                                }
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .height(seriesHeight)
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // collections
                            Icon(
                                imageVector = when(it.type) {
                                    "series" -> Icons.AutoMirrored.Default.List
                                    "season" -> Icons.AutoMirrored.Default.Article
                                    else -> Icons.AutoMirrored.Default.ViewList
                                },
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 5.dp)
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
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(
                                        onClick = archiveViewModel::toSeriesList,
                                    )
                                    .background(MaterialTheme.colorScheme.surfaceVariant,)
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