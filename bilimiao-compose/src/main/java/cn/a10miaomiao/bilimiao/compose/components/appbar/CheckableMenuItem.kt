package cn.a10miaomiao.bilimiao.compose.components.appbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cn.a10miaomiao.bilimiao.compose.R

/**
 * AppBar 可选中菜单项
 * 选中状态会显示主题色样式
 *
 * @param data 菜单项数据
 * @param checked 是否选中
 * @param themeColor 主题色（选中时使用）
 * @param onClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun CheckableMenuItem(
    data: MenuItemData,
    checked: Boolean,
    themeColor: Color,
    onClick: (MenuItemData) -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (checked) {
        themeColor
    } else {
        LocalContentColor.current.copy(alpha = 0.45f)
    }

    val backgroundColor = if (checked) {
        themeColor.copy(alpha = 0.1f)
    } else {
        Color.Transparent
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .width(AppBarConfig.MenuItemMinWidth)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = false),
                onClick = { onClick(data) }
            )
            .padding(vertical = AppBarConfig.NavigationIconPadding)
            .semantics {
                data.contentDescription?.let { contentDescription = it }
            },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 图标
            Box(
                modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    data.iconVector != null -> {
                        Icon(
                            imageVector = data.iconVector,
                            contentDescription = data.title,
                            tint = contentColor,
                            modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
                        )
                    }
                    data.iconResource != null -> {
                        Icon(
                            painter = painterResource(id = data.iconResource),
                            contentDescription = data.title,
                            tint = contentColor,
                            modifier = Modifier.size(AppBarConfig.MenuItemIconSize),
                        )
                    }
                }
            }

            // 标题
            if (data.title.isNotEmpty()) {
                Text(
                    text = data.title,
                    color = contentColor,
                    fontSize = AppBarConfig.TitleTextSize.value.sp,
                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(top = AppBarConfig.SubTitleMarginTop),
                )
            }

            // 副标题
            data.subTitle?.let { subTitle ->
                Text(
                    text = subTitle.replace("\n", " "),
                    color = contentColor.copy(alpha = 0.45f),
                    fontSize = AppBarConfig.SubTitleTextSize.value.sp,
                    textAlign = TextAlign.Center,
//                    modifier = Modifier.padding(top = AppBarConfig.SubTitleMarginTop),
                )
            }
        }
    }
}
