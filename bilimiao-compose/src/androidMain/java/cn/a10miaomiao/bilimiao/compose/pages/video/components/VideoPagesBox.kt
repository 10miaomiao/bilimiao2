package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Composable
fun VideoPagesBox(
    pages: List<bilibili.app.archive.v1.Page>,
    onPageClick: (bilibili.app.archive.v1.Page) -> Unit,
    onMoreClick: () -> Unit,
) {
    val playerStore by rememberInstance<PlayerStore>()
    val currentPlay by playerStore.stateFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        LazyRow(
            contentPadding = PaddingValues(
                start = 10.dp,
                top = 5.dp,
                bottom = 5.dp,
                end = 50.dp,
            ),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(pages.size, { pages[it].cid }) { index ->
                val page = pages[index]
                val isCurrentPlay = currentPlay.cid == page.cid.toString()
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .widthIn(
                            min = 60.dp,
                            max = 100.dp,
                        ),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = if (isCurrentPlay) BorderStroke(
                        1.dp, color = MaterialTheme.colorScheme.primary
                    ) else null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = {
                                onPageClick(page)
                            })
                            .padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = page.part,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isCurrentPlay) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isCurrentPlay) {
                                Text(
                                    text = "正在播放: P${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "P${index + 1} " + NumberUtil.converDuration(page.duration),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
        if (pages.size > 2) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background
                            ),
                            endX = 40f
                        )
                    ),
            ) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.Center),
                    onClick = onMoreClick,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }

}