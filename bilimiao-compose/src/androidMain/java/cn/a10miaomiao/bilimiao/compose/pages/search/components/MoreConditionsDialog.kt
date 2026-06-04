package cn.a10miaomiao.bilimiao.compose.pages.search.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.a10miaomiao.bilimiao.comm.store.RegionStore
import org.kodein.di.instance

data class MoreConditionsInfo(
    val timeType: Int,
    val durationList: List<Int>,
    val regionList: List<Int>
)

@Stable
internal class MoreConditionsDialogState(
    regionStore: RegionStore,
    val onConfirm: () -> Unit,
) {

    private val _visible = mutableStateOf(false)
    val visible get() = _visible.value

    val timeTypeList = listOf(
        0 to "不限",
        1 to "最近一天",
        7 to "最近一周",
        180 to "最近半年",
    )
    private val _timeTypeSelected = mutableIntStateOf(0)
    val timeTypeSelected get() = _timeTypeSelected.intValue

    val durationList = listOf(
        0 to "不限",
        1 to "0-10分钟",
        2 to "10-30分钟",
        3 to "30-60分钟",
        4 to "60分钟+",
    )
    private val _durationSelectedList = mutableStateOf(listOf(0))
    val durationSelectedList get() = _durationSelectedList.value

    val regionList = listOf(
        0 to "不限",
        *regionStore.state.regions.map {
            it.tid to it.name
        }.toTypedArray()
    )
    private val _regionSelectedList = mutableStateOf(listOf(0))
    val regionSelectedList get() = _regionSelectedList.value

    private var _data = MoreConditionsInfo(
        timeType = timeTypeSelected,
        durationList = durationSelectedList,
        regionList = regionSelectedList,
    )
    val data get() = _data

    fun handleSelectedTimeType(timeType: Int) {
        _timeTypeSelected.intValue = timeType
    }

    fun handleSelectedDuration(duration: Int) {
        if (duration == 0) {
            _durationSelectedList.value = listOf(0)
        } else if (durationSelectedList.indexOf(duration) == -1) {
            _durationSelectedList.value = listOf(
                *durationSelectedList.filter { it != 0 }.toTypedArray(), // 移除全部时长
                duration,
            )
        } else {
            _durationSelectedList.value = durationSelectedList.filter {
                it != duration
            }
            if (durationSelectedList.isEmpty()) {
                _durationSelectedList.value = listOf(0)
            }
        }
    }

    fun handleSelectedRegion(region: Int) {
        if (region == 0) {
            _regionSelectedList.value = listOf(0)
        } else if (regionSelectedList.indexOf(region) == -1) {
            _regionSelectedList.value = listOf(
                *regionSelectedList.filter { it != 0 }.toTypedArray(), // 移除全部时长
                region,
            )
        } else {
            _regionSelectedList.value = regionSelectedList.filter {
                it != region
            }
            if (regionSelectedList.isEmpty()) {
                _regionSelectedList.value = listOf(0)
            }
        }
    }

    fun open() {
        _timeTypeSelected.intValue = data.timeType
        _durationSelectedList.value = data.durationList
        _regionSelectedList.value = data.regionList
        _visible.value = true
    }

    fun close() {
        _visible.value = false
    }

    fun handleDismiss() {
        close()
    }

    fun handleConfirm() {
        _data = MoreConditionsInfo(
            timeType = timeTypeSelected,
            durationList = durationSelectedList,
            regionList = regionSelectedList,
        )
        onConfirm()
        close()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MoreConditionsDialog(
    state: MoreConditionsDialogState,
) {
    val timeTypeSelected = state.timeTypeSelected
    val durationSelectedList = state.durationSelectedList
    val regionSelectedList = state.regionSelectedList
    val leadingIcon: @Composable () -> Unit = {
        Icon(Icons.Default.Check, null)
    }
    if (state.visible) {
        AlertDialog(
            title = {
                Text("搜索筛选")
            },
            text = {
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier.verticalScroll(scrollState)
                ) {
                    Text(
                        "发布时间",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.timeTypeList.forEach { duration ->
                            val selected = timeTypeSelected == duration.first
                            FilterChip(
                                selected,
                                onClick = { state.run { handleSelectedTimeType(duration.first) } },
                                label = { Text(duration.second) },
                            )
                        }
                    }
                    Text(
                        "内容时长",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.durationList.forEach { duration ->
                            val selected = durationSelectedList.indexOf(duration.first) != -1
                            FilterChip(
                                selected,
                                onClick = { state.run { handleSelectedDuration(duration.first) } },
                                label = { Text(duration.second) },
                            )
                        }
                    }
                    Text(
                        "内容分区",
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        state.regionList.forEach { region ->
                            val selected = regionSelectedList.indexOf(region.first) != -1
                            FilterChip(
                                selected,
                                onClick = { state.run { handleSelectedRegion(region.first) } },
                                label = { Text(region.second) },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = state::handleConfirm
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = state::handleDismiss
                ) {
                    Text("取消")
                }
            },
            onDismissRequest = state::handleDismiss
        )
    }

}