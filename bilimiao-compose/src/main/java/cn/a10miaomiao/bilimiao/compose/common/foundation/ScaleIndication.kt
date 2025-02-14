package cn.a10miaomiao.bilimiao.compose.common.foundation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.invalidateDraw
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


object ScaleIndication : IndicationNodeFactory {

    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return DefaultScaleIndication(interactionSource)
    }

    override fun equals(other: Any?) = other === this

    override fun hashCode(): Int = -1

    private class DefaultScaleIndication(
        private val interactionSource: InteractionSource
    ) : Modifier.Node(), DrawModifierNode {
        var currentPressPosition: Offset = Offset.Zero
        val animatedScalePercent = Animatable(1f)

        private suspend fun animateToPressed(pressPosition: Offset) {
            currentPressPosition = pressPosition
            animatedScalePercent.animateTo(0.9f, spring())
        }

        private suspend fun animateToResting() {
            animatedScalePercent.animateTo(1f, spring())
        }

        override fun onAttach() {
            coroutineScope.launch {
                interactionSource.interactions.collectLatest { interaction ->
                    when (interaction) {
                        is PressInteraction.Press -> animateToPressed(interaction.pressPosition)
                        is PressInteraction.Release -> animateToResting()
                        is PressInteraction.Cancel -> animateToResting()
                    }
                }
            }
        }

        override fun ContentDrawScope.draw() {
            scale(
                scale = animatedScalePercent.value,
                pivot = currentPressPosition
            ) {
                this@draw.drawContent()
            }
        }
    }
}