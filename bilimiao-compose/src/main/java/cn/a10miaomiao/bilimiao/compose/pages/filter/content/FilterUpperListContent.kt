package cn.a10miaomiao.bilimiao.compose.pages.filter.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

private class FilterUpperListContentModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    private val filterStore by instance<FilterStore>()

    val stateFlow get() = filterStore.stateFlow

    fun deleteSelected(selectedMap: Map<Long, Int>) {
        val keywordList = selectedMap.keys.toList()
        if (keywordList.isEmpty()) {
            PopTip.show("未选择指定UP主")
        }
        filterStore.deleteUpper(keywordList)
    }
}

@Composable
internal fun FilterUpperListContent() {
    val viewModel: FilterUpperListContentModel = diViewModel()

    val state by viewModel.stateFlow.collectAsState()
    val filterUpperList = state.filterUpperList

    val selectedMap = remember {
        mutableStateMapOf<Long, Int>()
    }

    // -2为隐藏,-1为添加,>=0为编辑
    var inputMode by remember {
        mutableIntStateOf(-2)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filterUpperList.size > 0
                    && selectedMap.size == filterUpperList.size) {
                    TextButton(onClick = {
                        selectedMap.clear()
                    }) {
                        Text(text = "取消全选")
                    }
                } else {
                    TextButton(
                        onClick = {
                            selectedMap.putAll(filterUpperList.mapIndexed { index, upper ->
                                upper.mid to index
                            })
                        },
                        enabled = filterUpperList.size > 0
                    ) {
                        Text(text = "全选")
                    }
                }

                TextButton(
                    onClick = {
                        viewModel.deleteSelected(selectedMap.toMap())
                        selectedMap.clear()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                    enabled = selectedMap.size > 0
                ) {
                    Text(text = "删除选中")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(filterUpperList.size, { filterUpperList[it].mid }) { index ->
                    val upper = filterUpperList[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                inputMode = index
                            }
                    ) {
                        Checkbox(
                            checked = selectedMap.contains(upper.mid),
                            onCheckedChange = {
                                if (selectedMap.contains(upper.mid)) {
                                    selectedMap.remove(upper.mid)
                                } else {
                                    selectedMap[upper.mid] = index
                                }
                            }
                        )
                        Text(
                            text = upper.name + "(UID:${upper.mid})",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (filterUpperList.size == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(400.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                               Text(
                                   text = "空空如也\n似乎没有讨厌的人\n在UP主详情页更多菜单添加",
                                   color = MaterialTheme.colorScheme.outline,
                                   textAlign = TextAlign.Center,
                               )
                        }
                    }
                }
            }
        }
    }

}