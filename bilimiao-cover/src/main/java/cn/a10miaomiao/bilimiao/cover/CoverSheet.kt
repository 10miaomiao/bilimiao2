package cn.a10miaomiao.bilimiao.cover

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.common.BackHandler
import cn.a10miaomiao.bilimiao.compose.common.fetchOriginalImageBytes
import cn.a10miaomiao.bilimiao.compose.common.saveImageBytes
import cn.a10miaomiao.bilimiao.compose.components.image.MiaoAsyncImage
import cn.a10miaomiao.bilimiao.compose.components.image.MyImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerState
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.TransformItemView
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.VerticalDragType
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberPreviewerState
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.rememberTransformItemState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

/**
 * 封面查看弹层：展示封面、标题，并支持保存、复制链接与全屏预览。
 *
 * 自绘 overlay（进度 0 = 展开，1 = 收起）：遮罩透明度与弹层位移共用同一个进度，
 * 下滑时遮罩会跟手渐隐，收起动画结束后关闭页面。
 */
@Composable
internal fun CoverSheet(
    viewModel: CoverViewModel,
    onClose: () -> Unit,
) {
    val coverUrl by viewModel.coverUrl.collectAsState()
    val title by viewModel.title.collectAsState()
    val fileName by viewModel.fileName.collectAsState()
    val loading by viewModel.loading.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showPreview by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    // 0f = 完全展开，1f = 完全收起
    val progress = remember { Animatable(1f) }
    var sheetHeight by remember { mutableIntStateOf(0) }
    var opened by remember { mutableStateOf(false) }

    // 预览状态与封面共享，TransformItemView 会据此播放「封面 → 全屏」的过渡动画
    val previewerState = rememberPreviewerState(
        verticalDragType = VerticalDragType.Down,
        pageCount = { 1 },
        getKey = { 0 },
    )
    val coverItemState = rememberTransformItemState(
        intrinsicSize = Size(1280f, 720f),
    )

    fun toast(msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    fun close() {
        scope.launch {
            progress.animateTo(1f, tween(durationMillis = 220))
            onClose()
        }
    }

    fun copyCoverUrl() {
        val url = coverUrl ?: return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("imageUrl", url))
        toast("图片链接已复制到剪切板")
    }

    fun saveCoverBytes(bytes: ByteArray): Boolean {
        val name = "${fileName.ifBlank { "未命名" }}.jpg"
        val saved = saveImageBytes(name, bytes)
        toast(if (saved) "已保存至相册，文件名：$name" else "保存失败")
        return saved
    }

    fun saveCover() {
        val url = coverUrl
        if (url == null) {
            toast("图片未加载")
            return
        }
        scope.launch {
            val bytes = fetchOriginalImageBytes(url)
            if (bytes == null) {
                toast("原图下载失败")
                return@launch
            }
            saveCoverBytes(bytes)
        }
    }

    // 等弹层测量到高度后播放展开动画
    LaunchedEffect(sheetHeight) {
        if (sheetHeight > 0 && !opened) {
            progress.animateTo(
                0f,
                spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow,
                ),
            )
            opened = true
        }
    }

    BackHandler(enabled = !showPreview) { close() }

    Box(modifier = Modifier.fillMaxSize()) {
        // 遮罩：透明度跟随拖拽进度
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = (1f - progress.value) * 0.55f }
                .background(Color.Black)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { close() },
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .offset { IntOffset(0, (progress.value * sheetHeight).roundToInt()) }
                .onSizeChanged { sheetHeight = it.height }
                // 高度未测量前先隐藏，避免首帧闪现在底部
                .alpha(if (sheetHeight == 0) 0f else 1f)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(sheetHeight) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (progress.value > 0.25f) {
                                    progress.animateTo(1f, tween(durationMillis = 200))
                                    onClose()
                                } else {
                                    progress.animateTo(
                                        0f,
                                        spring(
                                            dampingRatio = Spring.DampingRatioNoBouncy,
                                            stiffness = Spring.StiffnessMediumLow,
                                        ),
                                    )
                                }
                            }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            if (sheetHeight > 0) {
                                scope.launch {
                                    progress.snapTo(
                                        (progress.value + dragAmount / sheetHeight)
                                            .coerceIn(0f, 1f),
                                    )
                                }
                            }
                        },
                    )
                }
                .navigationBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(top = 12.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 拖拽把手
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)),
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "查看封面 · $fileName",
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (coverUrl != null) {
                    TransformItemView(
                        key = 0,
                        itemState = coverItemState,
                        transformState = previewerState,
                    ) {
                        MiaoAsyncImage(
                            model = coverUrl,
                            contentDescription = "封面",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp)
                                .clickable { showPreview = true },
                            contentScale = ContentScale.Fit,
                        )
                    }
                }
                if (loading || coverUrl == null) {
                    CircularProgressIndicator()
                }
            }
            Text(
                text = title,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 40.dp)
                    .padding(horizontal = 10.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilledTonalButton(
                    onClick = { saveCover() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "保存封面",
                            style = MaterialTheme.typography.labelLarge,
                        )
                        Text(
                            text = "文件名：${fileName.ifBlank { "未命名" }}.jpg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Box {
                    FilledTonalIconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(52.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多",
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("复制封面链接") },
                            onClick = {
                                menuExpanded = false
                                copyCoverUrl()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("查看更多") },
                            onClick = {
                                menuExpanded = false
                                viewModel.openMore()
                            },
                        )
                    }
                }
            }
        }
    }

    val previewUrl = coverUrl
    if (showPreview && previewUrl != null) {
        // 同一个窗口内叠加，背景由预览器自身按进入动画渐显
        val imageModels = remember(previewUrl) {
            listOf(PreviewImageModel(1280f, 720f, previewUrl, previewUrl))
        }
        val previewerStateHolder = remember(previewerState, imageModels) {
            ImagePreviewerState(
                previewerState = previewerState,
                imageModels = imageModels,
            )
        }
        var previewOpened by remember(previewerState) { mutableStateOf(false) }

        // 封面已注册共享元素，这里会播放「封面 → 全屏」的过渡动画
        LaunchedEffect(previewerState) {
            previewerState.enterTransform(0)
        }
        // 进入动画完成后标记；退出时等预览器的图层动画彻底结束再卸载，避免封面还没恢复就出现空白
        LaunchedEffect(previewerState.canClose, previewerState.animating) {
            if (previewerState.canClose) {
                previewOpened = true
            } else if (previewOpened && !previewerState.animating) {
                // 多等一帧，让共享元素（封面）先恢复显示
                delay(32)
                showPreview = false
            }
        }
        BackHandler(enabled = true) {
            scope.launch { previewerState.exitTransform() }
        }
        MyImagePreviewer(
            imagePreviewerState = previewerStateHolder,
            // 下载行为与外面的「保存封面」保持一致（文件名、保存位置、提示）
            onSaveImage = { _, bytes ->
                // 预览器的下载在后台线程回调，保存与提示切回主线程
                withContext(Dispatchers.Main) {
                    saveCoverBytes(bytes)
                }
                true
            },
        )
    }
}
