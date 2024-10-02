package cn.a10miaomiao.bilimiao.compose.pages.filter.content

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance


internal class FilterTagListContentModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val fragment by instance<Fragment>()

    private val filterStore by instance<FilterStore>()

    val stateFlow get() = filterStore.stateFlow

    fun addTag(text: String) {
        val filterTagList = stateFlow.value.filterTagList
        if (filterTagList.indexOf(text) == -1) {
            filterStore.addTag(text)
        } else {
            PopTip.show("该标签已存在")
        }
    }

    fun setTag(old: String, new: String) {
        filterStore.setTag(old, new)
    }

    fun deleteSelected(selectedMap: Map<String, Int>) {
        val tagList = selectedMap.keys.toList()
        if (tagList.isEmpty()) {
            PopTip.show("未选择标签")
        }
        filterStore.deleteTag(tagList)
    }
}

@Composable
internal fun FilterTagListContent() {
    val viewModel: FilterTagListContentModel = diViewModel()

    val state by viewModel.stateFlow.collectAsState()
    val filterTagList = state.filterTagList

    val selectedMap = remember {
        mutableStateMapOf<String, Int>()
    }

    // -2为隐藏,-1为添加,>=0为编辑
    var inputMode by remember {
        mutableIntStateOf(-2)
    }
    var inputText by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        Text(
            text = "此功能为实验性功能，仅对首页推荐和热门列表生效，开启标签屏蔽后列表加载可能有性能问题",
            textAlign = TextAlign.Center,
            color = Color.Red,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filterTagList.size > 0
                    && selectedMap.size == filterTagList.size) {
                    TextButton(onClick = {
                        selectedMap.clear()
                    }) {
                        Text(text = "取消全选")
                    }
                } else {
                    TextButton(
                        onClick = {
                            selectedMap.putAll(filterTagList.mapIndexed { index, s ->
                                s to index
                            })
                        },
                        enabled = filterTagList.size > 0,
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
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    inputMode = -1
                }) {
                    Text(text = "添加")
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
            ) {
                items(filterTagList.size, { filterTagList[it] }) { index ->
                    val tag = filterTagList[index]
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                inputMode = index
                                inputText = tag
                            }
                    ) {
                        Checkbox(
                            checked = selectedMap.contains(tag),
                            onCheckedChange = {
                                if (selectedMap.contains(tag)) {
                                    selectedMap.remove(tag)
                                } else {
                                    selectedMap[tag] = index
                                }
                            }
                        )
                        Text(
                            text = tag,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (filterTagList.size == 0) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .height(400.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "空空如也\n去添加标签吧\n屏蔽标签会导致加载变慢!",
                                color = MaterialTheme.colorScheme.outline,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }

    var errorText by remember {
        mutableStateOf("")
    }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(inputMode) {
        if (inputMode > -2) {
            launch {
                focusRequester.requestFocus()
            }
        }

    }
    fun handleDismiss() {
        inputMode = -2
        inputText = ""
        errorText = ""
    }

    fun handleConfirm() {
        if (inputText.isBlank()) {
            errorText = "请输入标签"
            return
        }
        if (inputMode < 0) {
            viewModel.addTag(inputText)
        } else {
            val oldWord = filterTagList[inputMode]
            viewModel.setTag(oldWord, inputText)
        }
        handleDismiss()
    }

    if (inputMode > -2) {
        AlertDialog(
            onDismissRequest = ::handleDismiss,
            title = {
                if (inputMode < 0) {
                    Text(text = "添加屏蔽标签")
                } else {
                    Text(text = "编辑屏蔽标签")
                }
            },
            text = {
                Column {
                    TextField(
                        label = {
                            Text(text = "标签")
                        },
                        isError = errorText.isNotBlank(),
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            errorText = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = { handleConfirm() }
                        ),
                    )
                }

            },
            confirmButton = {
                TextButton(
                    onClick = ::handleConfirm,
                ) {
                    Text("确认添加")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = ::handleDismiss,
                ) {
                    Text("取消")
                }
            }
        )
    }

}