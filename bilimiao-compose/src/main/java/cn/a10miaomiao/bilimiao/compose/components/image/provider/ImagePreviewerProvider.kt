package cn.a10miaomiao.bilimiao.compose.components.image.provider

import androidx.activity.compose.BackHandler
import androidx.collection.mutableIntSetOf
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.LayoutDirection
import cn.a10miaomiao.bilimiao.compose.components.image.previewer.ImagePreviewer
import cn.a10miaomiao.bilimiao.compose.components.image.viewer.AnyComposable
import cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer.PreviewerState
import com.bumptech.glide.Glide
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Stable
class PreviewImageModel(
    val width: Float,
    val height: Float,
    val previewUrl: String,
    val originalUrl: String,
)

@Stable
class ImagePreviewerState(
    val previewerState: PreviewerState,
    val imageModels: List<PreviewImageModel>,
    val onImageLoaded: (Int) -> Unit = {},
)

class ImagePreviewerController {
    private val _imagePreviewerState = mutableStateOf<ImagePreviewerState?>(null)
    val imagePreviewerState: State<ImagePreviewerState?> get() = _imagePreviewerState

    private var _enterIndex = mutableIntStateOf(-1)
    val enterIndex: IntState get() =  _enterIndex
    var enterAnimationSpec: AnimationSpec<Float>? = null
        private set

    private val _imageLoaded = mutableStateOf(mutableSetOf<String>())

    fun enterTransform(
        state: PreviewerState,
        models: List<PreviewImageModel>,
        index: Int = 0,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        _imagePreviewerState.value = ImagePreviewerState(
            previewerState = state,
            imageModels = models,
            onImageLoaded = {
                setImageLoaded(models[it].originalUrl)
            }
        )
        _enterIndex.value = index
        enterAnimationSpec = animationSpec
    }

    internal fun clearEnter() {
        _enterIndex.value = -1
        enterAnimationSpec = null
    }

    internal fun clearState() {
        _imagePreviewerState.value = null
    }

    private fun setImageLoaded(imageUrl: String) {
        _imageLoaded.value = _imageLoaded.value
            .toMutableSet()
            .apply {
                if (size > 500) clear()
                add(imageUrl)
            }
    }

    fun isImageLoaded(imageUrl: String): Boolean {
        return _imageLoaded.value.contains(imageUrl)
    }

}

internal val LocalImagePreviewerController = staticCompositionLocalOf<ImagePreviewerController> {
    error("CompositionLocal LocalImagePreviewerController not present")
}

@Composable
fun localImagePreviewerController() = LocalImagePreviewerController.current

@Composable
fun ImagePreviewerProvider(
    contentPadding: PaddingValues = PaddingValues(),
    previewer: @Composable (ImagePreviewerState, PaddingValues) -> Unit,
    content: @Composable () -> Unit
) {
    val scope = rememberCoroutineScope()
    val controller = remember {
        ImagePreviewerController()
    }
    CompositionLocalProvider(
        LocalImagePreviewerController provides controller,
        content = content,
    )
    val imagePreviewerState = controller.imagePreviewerState.value
    if (imagePreviewerState != null) {
        val previewerState = imagePreviewerState.previewerState
        previewer(imagePreviewerState, contentPadding)
        val enterIndex = controller.enterIndex.value
        LocalDensity.current.apply {
            LaunchedEffect(enterIndex) {
                if (enterIndex != -1) {
                    val left = contentPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx()
                    val right = contentPadding.calculateLeftPadding(LayoutDirection.Ltr).toPx()
                    val top = contentPadding.calculateTopPadding().toPx()
                    val bottom = contentPadding.calculateBottomPadding().toPx()
                    previewerState.offsetSize = Size(
                        height = (top - bottom).div(2f),
                        width = (left - right).div(2f),
                    )
                    previewerState.enterTransform(
                        enterIndex,
                        controller.enterAnimationSpec
                    )
                    controller.clearEnter()
                }
            }
        }
        LaunchedEffect(
            previewerState.canClose,
            previewerState.animating,
            enterIndex,
        ) {
            if (enterIndex == -1 &&
                !previewerState.canClose &&
                !previewerState.animating) {
                controller.clearState()
            }
        }
        BackHandler {
            if (!previewerState.animating) scope.launch {
                previewerState.exitTransform()
            }
        }
    }
}