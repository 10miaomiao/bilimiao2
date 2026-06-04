package cn.a10miaomiao.bilimiao.compose.components.layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@OptIn(ExperimentalSharedTransitionApi::class)
@Stable
class DataDrivenNavigatorScope(
    val sharedTransitionScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope,
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun <T> DataDrivenNavigator(
    modifier: Modifier = Modifier,
    data: T?,
    dataKey: ((data: T) -> Any?) = { it },
    dataContent: @Composable DataDrivenNavigatorScope.(T) -> Unit,
    content: @Composable DataDrivenNavigatorScope.() -> Unit,
) {
    SharedTransitionLayout(
        modifier = modifier,
    ) {
        AnimatedContent(
            targetState = data,
            contentKey = { it?.let(dataKey) },
            label = "DataDrivenNavigator",
        ) { targetState ->
            val scope = remember {
                DataDrivenNavigatorScope(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedVisibilityScope = this@AnimatedContent
                )
            }
            if (targetState == null) {
                content(scope)
            } else {
                dataContent(scope, targetState)
            }
        }
    }
}