package cn.a10miaomiao.bilimiao.compose.components.image

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.rememberMyMenu
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.provider.PreviewImageModel
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.AnyComposable
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import com.a10miaomiao.bilimiao.comm.mypage.MenuKeys
import com.skydoves.landscapist.glide.GlideImage

@Composable
fun MyImagePreviewer(
    previewerState: PreviewerState,
    imageModels: List<PreviewImageModel>,
    contentPadding: PaddingValues,
) {
    val pagerState = previewerState.pagerState
    PageConfig(
        title = "查看图片",
        menu = rememberMyMenu {
            myItem {
                key = MenuKeys.save
                title = "保存原图"
                iconFileName = "ic_baseline_save_24"
            }
        }
    )
    ImagePreviewer(
        contentPadding = contentPadding,
        state = previewerState,
        imageLoader = { page ->
            val model = imageModels[page]
            return@ImagePreviewer Pair(
                AnyComposable(
                    composable = {
                        GlideImage(
                            imageModel = model.originalUrl,
                            loading = {
                                GlideImage(model.previewUrl)
                            }
                        )
                    }
                ),
                Size(model.width, model.height)
            )
        }
    )
}