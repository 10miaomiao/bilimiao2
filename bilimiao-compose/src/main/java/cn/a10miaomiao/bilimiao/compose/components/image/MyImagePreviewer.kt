package cn.a10miaomiao.bilimiao.compose.components.image

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.ImagePreviewerState
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.AnyComposable
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.a10miaomiao.bilimiao.comm.mypage.MyPageMenu
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.utils.ImageSaveUtil
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.GlideSubcomposition
import com.bumptech.glide.integration.compose.RequestState
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.compose.rememberInstance
import java.io.File

private class MyImagePreviewerController(
    val activity: FragmentActivity,
    val imagePreviewerState: ImagePreviewerState,
) {

    val isDownloading = mutableStateOf(false)

    fun saveImageFile(
        imageUrl: String,
    ) {
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

    fun copyImageUrl(imageUrl: String) {
        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("imageUrl", imageUrl)
        clipboardManager.setPrimaryClip(clipData)
        PopTip.show("图片链接已复制到剪切板")
    }

    fun shareImage(imageUrl: String) {
        val target = object : CustomTarget<File>() {
            override fun onResourceReady(
                resource: File,
                transition: Transition<in File>?
            ) {
                if (isDownloading.value) {

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

    fun menuItemClick(view: View, menuItem: MenuItemPropInfo) {
        val page = imagePreviewerState.previewerState.currentPage
        val model = imagePreviewerState.imageModels[page]
        when (menuItem.key) {
            MenuKeys.save -> {
                saveImageFile(model.originalUrl)
            }
            1 -> {
                copyImageUrl(model.originalUrl)
            }
            2 -> {
                shareImage(model.originalUrl)
            }
        }
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
    val activity: FragmentActivity by rememberInstance()
    val controller = remember(imagePreviewerState) {
        MyImagePreviewerController(activity, imagePreviewerState)
    }
    val pageConfigId = PageConfig(
        title = "查看图片",
        menu = rememberMyMenu {
            myItem {
                key = MenuKeys.more
                title = "更多"
                iconFileName = "ic_more_vert_grey_24dp"
                childMenu = myMenu {
                    myItem {
                        key = 1
                        title = "复制图片链接"
                    }
//                    myItem {
//                        key = 2
//                        title = "分享图片"
//                    }
                }
            }
            myItem {
                key = MenuKeys.save
                title = "保存图片"
                iconFileName = "ic_baseline_save_24"
            }
        }
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = controller::menuItemClick,
    )
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
        }
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

