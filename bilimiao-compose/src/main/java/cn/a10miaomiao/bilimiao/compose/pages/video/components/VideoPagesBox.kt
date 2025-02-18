package cn.a10miaomiao.bilimiao.compose.pages.video.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a10miaomiao.bilimiao.comm.store.PlayerStore
import org.kodein.di.compose.rememberInstance
import org.kodein.di.instance

@Composable
fun VideoPagesBox(
    pages: List<bilibili.app.archive.v1.Page>,
    onPageClick: (bilibili.app.archive.v1.Page) -> Unit,
) {
    val playerStore by rememberInstance<PlayerStore>()
    val currentPlay by playerStore.stateFlow.collectAsState()

    Box {
        LazyRow(
            contentPadding = PaddingValues(
                start = 10.dp,
                top = 5.dp,
                bottom = 5.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            items(pages) { page ->
                val isCurrentPlay = currentPlay.cid == page.cid.toString()
                Surface(
                    modifier = Modifier.fillMaxWidth()
                        .heightIn(min = 50.dp),
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
                            .padding(10.dp),
                    ) {
                        Text(
                            text = page.part,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }

}