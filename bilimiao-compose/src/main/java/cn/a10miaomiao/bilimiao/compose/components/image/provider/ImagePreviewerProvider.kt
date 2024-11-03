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
    val contentPadding: PaddingValues
)

class ImagePreviewerController {
    private var _previewerState = mutableStateOf<PreviewerState?>(null)
    val previewerState: State<PreviewerState?> get() = _previewerState

    private var _imageModels: List<PreviewImageModel> = listOf()
    val imageModels get() = _imageModels

    private var _enterIndex = mutableIntStateOf(-1)
    val enterIndex: IntState get() =  _enterIndex
    var enterAnimationSpec: AnimationSpec<Float>? = null
        private set

    fun enterTransform(
        state: PreviewerState,
        models: List<PreviewImageModel>,
        index: Int = 0,
        animationSpec: AnimationSpec<Float>? = null,
    ) {
        _previewerState.value = state
        _imageModels = models
        _enterIndex.value = index
        enterAnimationSpec = animationSpec
    }

    internal fun clearEnter() {
        _enterIndex.value = -1
        enterAnimationSpec = null
    }

    internal fun clearState() {
        _previewerState.value = null
        _imageModels = listOf()
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
    previewer: @Composable (ImagePreviewerState) -> Unit,
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
    val previewerState = controller.previewerState.value
    if (previewerState != null) {
        previewer(
            ImagePreviewerState(
                previewerState = previewerState,
                imageModels = controller.imageModels,
                contentPadding = contentPadding,
            )
        )
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