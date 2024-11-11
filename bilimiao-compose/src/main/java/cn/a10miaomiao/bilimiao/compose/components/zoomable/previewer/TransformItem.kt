package cn.a10miaomiao.bilimiao.compose.components.zoomable.previewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * 页面根据Key获取对应小图转换的状态数据
 */
val LocalTransformItemStateMap =
    compositionLocalOf<ItemStateMap> { ItemStateMap() }

@Stable
class ItemStateMap {
    val map = mutableStateMapOf<Any, List<TransformItemState>>()

    operator fun set(key: Any, value: TransformItemState) {
        val originalValue = map[key]
        if (originalValue == null) {
            map[key] = listOf(value)
        } else {
            map[key] = listOf(
                *originalValue.toTypedArray(),
                value
            )
        }
    }

    operator fun get(key: Any): TransformItemState? {
        return map[key]?.lastOrNull()
    }

    fun remove(key: Any, state: TransformItemState) {
        val value = map[key] ?: return
        if (value.size > 1) {
            map[key] = value.filter {
                it !== state
            }
        } else {
            map.remove(key)
        }
    }
}
//typealias ItemStateMap = MutableMap<Any, TransformItemState>

/**
 * 在compose中获取一个TransformItemState的方式
 *
 * @param scope 协程作用域
 * @param checkInBound 判断当前图片是否在显示范围内，可以为空
 * @param intrinsicSize 图片的固有大小，必须要有值且确定才能正常显示
 * @return TransformItemState
 */
@Composable
fun rememberTransformItemState(
    scope: CoroutineScope = rememberCoroutineScope(),
    checkInBound: (TransformItemState.() -> Boolean)? = null,
    intrinsicSize: Size? = null,
): TransformItemState {
    val transformItemState =
        remember {
            TransformItemState(
                scope = scope,
                checkInBound = checkInBound,
            )
        }
    transformItemState.intrinsicSize = intrinsicSize
    return transformItemState
}

/**
 * TransformItem的状态与控制对象
 *
 * @property key 当前TransformItem的唯一标识
 * @property blockCompose 实际TransformItem中显示的内容
 * @property scope 协程作用域
 * @property blockPosition TransformItem的绝对位置
 * @property blockSize TransformItem的大小
 * @property intrinsicSize 传入blockCompose的固有大小
 * @property checkInBound 判断当前TransformItem是否在显示范围内的方法
 */
class TransformItemState(
    var key: Any = Unit,
    var blockCompose: (@Composable (Any) -> Unit) = {},
    var scope: CoroutineScope,
    var blockPosition: Offset = Offset.Zero,
    var blockSize: IntSize = IntSize.Zero,
    var intrinsicSize: Size? = null,
    var checkInBound: (TransformItemState.() -> Boolean)? = null,
) {

    private fun checkItemInMap(itemMap: ItemStateMap) {
        if (checkInBound == null) return
        if (checkInBound!!.invoke(this)) {
            addItem(itemMap = itemMap)
        } else {
            removeItem(itemMap = itemMap)
        }
    }

    /**
     * 位置和大小发生变化时
     * @param position Offset
     * @param size IntSize
     */
    internal fun onPositionChange(position: Offset, size: IntSize, itemMap: ItemStateMap) {
        blockPosition = position
        blockSize = size
        scope.launch {
            checkItemInMap(itemMap)
        }
    }

    /**
     * 判断item是否在所需范围内，返回true，则添加该item到map，返回false则移除
     * @param checkInBound Function0<Boolean>
     */
    fun checkIfInBound(itemMap: ItemStateMap, checkInBound: () -> Boolean) {
        if (checkInBound()) {
            addItem(itemMap = itemMap)
        } else {
            removeItem(itemMap = itemMap)
        }
    }

    /**
     * 添加item到map上
     * @param key Any?
     */
    fun addItem(key: Any? = null, itemMap: ItemStateMap) {
        // TODO mutex
        val currentKey = key ?: this.key
        if (checkInBound != null) return
        itemMap[currentKey] = this
    }

    /**
     * 从map上移除item
     * @param key Any?
     */
    fun removeItem(key: Any? = null, itemMap: ItemStateMap) {
        // TODO mutex
        val currentKey = key ?: this.key
        if (checkInBound != null) return
        itemMap.remove(currentKey, this)
    }
}

/**
 * 用于实现Previewer变换效果的小图装载容器
 *
 * @param modifier 图层修饰
 * @param key 唯一标识
 * @param itemState 该组件的状态与控制对象
 * @param itemVisible 该组件的可见性
 * @param content 需要显示的实际内容
 */
@Composable
fun TransformItemView(
    modifier: Modifier = Modifier,
    key: Any,
    itemState: TransformItemState = rememberTransformItemState(),
    itemStateMap: ItemStateMap = LocalTransformItemStateMap.current,
    itemVisible: Boolean,
    content: @Composable (Any) -> Unit,
) {
    val scope = rememberCoroutineScope()
    itemState.key = key
    itemState.blockCompose = content
    DisposableEffect(key) {
        // 这个composable加载时添加到map
        scope.launch {
            itemState.addItem(itemMap = itemStateMap)
        }
        onDispose {
            // composable退出时从map移除
            itemState.removeItem(itemMap = itemStateMap)
        }
    }
    Box(
        modifier = modifier
            .onGloballyPositioned {
                itemState.onPositionChange(
                    position = it.positionInRoot(),
                    size = it.size,
                    itemMap = itemStateMap,
                )
            }
            .fillMaxSize()
            .alpha(if (itemVisible) 1f else 0f)
    ) {
        itemState.blockCompose(key)
    }
}