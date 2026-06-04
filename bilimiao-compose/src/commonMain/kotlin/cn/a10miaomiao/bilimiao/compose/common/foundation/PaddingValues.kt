package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Stable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Stable
private class MergingPaddingValues(
    private val padding1: PaddingValues,
    private val padding2: PaddingValues
) : PaddingValues {
    override fun calculateBottomPadding(): Dp {
        return padding1.calculateBottomPadding() + padding2.calculateBottomPadding()
    }

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp {
        return padding1.calculateLeftPadding(layoutDirection) + padding2.calculateLeftPadding(layoutDirection)
    }

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp {
        return padding1.calculateRightPadding(layoutDirection) + padding2.calculateRightPadding(layoutDirection)
    }

    override fun calculateTopPadding(): Dp {
        return padding1.calculateTopPadding() + padding2.calculateTopPadding()
    }
}

fun PaddingValues.add(padding: PaddingValues): PaddingValues {
    return MergingPaddingValues(this, padding)
}