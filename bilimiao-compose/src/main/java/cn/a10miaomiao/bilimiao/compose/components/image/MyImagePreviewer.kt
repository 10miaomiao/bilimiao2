package cn.a10miaomiao.bilimiao.compose.components.image

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.defaultPreviewBackground
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformLayerScope
import com.a10miaomiao.bilimiao.comm.utils.ImageSaveUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.compose.rememberInstance
import java.io.File

private class MyImagePreviewerController(
    val activity: Activity,
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
        val target = object : CustomTarget<File>() {
            override fun onResourceReady(
                resource: File,
                transition: Transition<in File>?
            ) {
                if (isDownloading.value) {
                    ImageSaveUtil.saveImage(
                        activity,
                        ImageSaveUtil.getFileName(imageUrl),
                        resource,
                    )
                    isDownloading.value = false
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                PopTip.show("原图下载失败")
                isDownloading.value = false
            }
        }
        Glide.with(activity)
            .asFile()
            .load(imageUrl)
            .into(target)
        isDownloading.value = true
    }

    fun copyImageUrl() {
        val imageUrl = getCurrentImageUrl()
        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("imageUrl", imageUrl)
        clipboardManager.setPrimaryClip(clipData)
        PopTip.show("图片链接已复制到剪切板")
    }

    fun shareImage() {
        val imageUrl = getCurrentImageUrl()
        val target = object : CustomTarget<File>() {
            override fun onResourceReady(
                resource: File,
                transition: Transition<in File>?
            ) {
                if (isDownloading.value) {
                    val uri = ImageSaveUtil.getImageUri(activity, resource)
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    activity.startActivity(Intent.createChooser(shareIntent, "分享图片"))
                    isDownloading.value = false
                }
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                PopTip.show("图片加载失败")
                isDownloading.value = false
            }
        }
        Glide.with(activity)
            .asFile()
            .load(imageUrl)
            .into(target)
        isDownloading.value = true
    }

    fun cancelDownloading() {
        isDownloading.value = false
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun MyImagePreviewer(
    imagePreviewerState: ImagePreviewerState,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val activity: Activity by rememberInstance()
    val controller = remember(imagePreviewerState) {
        MyImagePreviewerController(activity, imagePreviewerState)
    }
    var showMoreMenu by remember { mutableStateOf(false) }

    ImagePreviewer(
        contentPadding = contentPadding,
        state = imagePreviewerState.previewerState,
        imageLoader = { page ->
            val model = imagePreviewerState.imageModels[page]
            val imageUrl = model.originalUrl
            val painterState = remember { mutableStateOf<Painter?>(null) }
            GlideSubcomposition(imageUrl) {
                if (state is RequestState.Success) {
                    painterState.value = painter
                    // 设置原图已下载标志
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
                    modifier = Modifier.fillMaxSize(),
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
                                DropdownMenuItem(
                                    text = { Text("分享图片") },
                                    onClick = {
                                        showMoreMenu = false
                                        controller.shareImage()
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Share,
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

