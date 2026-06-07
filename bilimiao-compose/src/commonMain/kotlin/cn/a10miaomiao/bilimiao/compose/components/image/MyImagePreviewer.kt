package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.fetchOriginalImageBytes
import cn.a10miaomiao.bilimiao.compose.common.getImageFileName
import cn.a10miaomiao.bilimiao.compose.common.saveImageBytes
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.defaultPreviewBackground
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformLayerScope
import cn.a10miaomiao.bilimiao.compose.platform.LocalPlatformContext
import cn.a10miaomiao.bilimiao.compose.platform.PlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.AsyncImagePainter
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private class MyImagePreviewerController(
    val coroutineScope: CoroutineScope,
    val platformContext: PlatformContext,
    val imagePreviewerState: ImagePreviewerState,
) {
    val isDownloading = mutableStateOf(false)

    private fun getCurrentImageUrl(): String {
        val page = imagePreviewerState.previewerState.currentPage
        val model = imagePreviewerState.imageModels[page]
        return model.originalUrl
    }

    fun saveImageFile() {
        val imageUrl = getCurrentImageUrl()
        isDownloading.value = true
        coroutineScope.launch(Dispatchers.Default) {
            try {
                val fileName = getImageFileName(imageUrl)
                val bytes = fetchOriginalImageBytes(imageUrl)
                if (bytes != null && bytes.isNotEmpty()) {
                    saveImageBytes(fileName, bytes)
                } else {
                    GlobalToaster.show("原图下载失败")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                GlobalToaster.show("原图下载失败")
            } finally {
                isDownloading.value = false
            }
        }
    }

    fun copyImageUrl() {
        val imageUrl = getCurrentImageUrl()
        platformContext.copyToClipboard(imageUrl)
        GlobalToaster.show("图片链接已复制到剪切板")
    }

    fun cancelDownloading() {
        isDownloading.value = false
    }
}

@Composable
fun MyImagePreviewer(
    imagePreviewerState: ImagePreviewerState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val coroutineScope = rememberCoroutineScope()
    val platformContext = LocalPlatformContext.current
    val controller = remember(imagePreviewerState) {
        MyImagePreviewerController(coroutineScope, platformContext, imagePreviewerState)
    }
    var showMoreMenu by remember { mutableStateOf(false) }

    ImagePreviewer(
        contentPadding = contentPadding,
        state = imagePreviewerState.previewerState,
        imageLoader = { page ->
            val model = imagePreviewerState.imageModels[page]
            val imageUrl = model.originalUrl
            val painterState = remember { mutableStateOf<Painter?>(null) }
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = null,
            ) {
                if (painter.state is AsyncImagePainter.State.Success) {
                    painterState.value = painter
                    imagePreviewerState.onImageLoaded(page)
                }
            }
            return@ImagePreviewer Pair(
                painterState.value,
                Size(model.width, model.height)
            )
        },
        previewerLayer = TransformLayerScope(
            background = defaultPreviewBackground,
            foreground = {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .padding(
                            WindowInsets.safeDrawing
                                .only(WindowInsetsSides.Bottom + WindowInsetsSides.End)
                                .asPaddingValues()
                        )
                        .padding(bottom = 40.dp),
                    contentAlignment = Alignment.BottomEnd,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CapsuleIconButton(
                            icon = Icons.Default.Download,
                            contentDescription = "保存图片",
                            onClick = controller::saveImageFile,
                        )
                        Box {
                            CapsuleIconButton(
                                icon = Icons.Default.MoreVert,
                                contentDescription = "更多",
                                onClick = { showMoreMenu = true },
                            )
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false },
                            ) {
                                DropdownMenuItem(
                                    text = { Text("复制图片链接") },
                                    onClick = {
                                        showMoreMenu = false
                                        controller.copyImageUrl()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            },
        ),
    )
    if (controller.isDownloading.value) {
        AlertDialog(
            onDismissRequest = controller::cancelDownloading,
            confirmButton = {
                TextButton(onClick = controller::cancelDownloading) {
                    Text("取消")
                }
            },
            title = {
                Text("正在下载图片")
            },
            text = {
                LinearProgressIndicator()
            }
        )
    }
}

@Composable
private fun CapsuleIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(40.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}
