package cn.a10miaomiao.bilimiao.compose.components.start

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.components.dialogs.AutoSheetDialog
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestInfo
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestType
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import com.kongzue.dialogx.dialogs.PopTip
import org.kodein.di.compose.rememberInstance

@Composable
fun SearchInputDialog(
    initKeyword: String,
    initMode: Int,
    selfSearchName: String?,
    onDismissRequest: () -> Unit,
) {
    val viewModel: SearchInputViewModel = cn.a10miaomiao.bilimiao.compose.common.diViewModel()
    val pageNavigation: PageNavigation by rememberInstance()
    val activity: Activity by rememberInstance()

    var text by remember { mutableStateOf(initKeyword) }
    var mode by remember { mutableStateOf(initMode) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(text) {
        viewModel.loadSuggestData(text, text)
    }

    val suggestList by viewModel.suggestListFlow.collectAsState()

    var pendingDelete by remember { mutableStateOf<String?>(null) }
    var showClearAll by remember { mutableStateOf(false) }

    fun startSearch(keyword: String) {
        if (keyword.isEmpty()) {
            PopTip.show("请输入ID或关键字")
            return
        }
        viewModel.addSearchHistory(keyword)
        if (mode == 0) {
            pageNavigation.navigate(SearchResultPage(keyword))
        } else {
//            activity.searchSelfPage(keyword)
        }
        onDismissRequest()
    }

    AutoSheetDialog(
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        onDismiss = onDismissRequest,
    ) {
        LaunchedEffect(Unit) {
            // Slight delay to wait for sheet animation/start of composition
            kotlinx.coroutines.delay(100)
            focusRequester.requestFocus()
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Suggestions list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(suggestList) { item: SuggestInfo ->
                    ListItem(
                        modifier = Modifier
                            .clickable {
                                when (item.type) {
                                    SuggestType.AV -> pageNavigation.navigateByUri(Uri.parse("bilimiao://video/${item.value}"))
                                    SuggestType.SS -> pageNavigation.navigateByUri(Uri.parse("bilimiao://bangumi/${item.value}"))
                                    else -> startSearch(item.value)
                                }
                            },
                        headlineContent = { Text(text = item.text) },
                        leadingContent = {
                            if (item.type == SuggestType.HISTORY) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        trailingContent = {
                            if (item.type == SuggestType.HISTORY) {
                                TextButton(onClick = { pendingDelete = item.value }) {
                                    Text("删除")
                                }
                            }
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Divider()
                }
            }

            // Bottom input and actions bar
            Surface(
                tonalElevation = 3.dp,
                shadowElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = mode == 0,
                            onClick = { mode = 0 },
                            label = { Text("全站搜索") }
                        )
                        FilterChip(
                            selected = mode == 1,
                            onClick = { mode = 1 },
                            label = { Text(selfSearchName ?: "当前页内") }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            value = text,
                            onValueChange = { text = it },
                            singleLine = true,
                            placeholder = { Text("输入ID或关键字") },
                            leadingIcon = { Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            ) },
                            trailingIcon = {
                                if (text.isNotEmpty()) {
                                    IconButton(onClick = { text = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "清空")
                                    }
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = { startSearch(text) }
                            ),
                            shape = MaterialTheme.shapes.large,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            )
                        )
//                        IconButton(onClick = { startSearch(text) }) {
//                            Icon(Icons.Default.Search, contentDescription = "搜索")
//                        }
                    }
                }
            }
        }
    }

    pendingDelete?.let { value ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("确认删除，喵~") },
            text = { Text("将删除搜索历史关键字") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteSearchHistory(value)
                    PopTip.show("已删除")
                    pendingDelete = null
                }) { Text("确定") }
            },
            dismissButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { showClearAll = true; pendingDelete = null }) { Text("清空全部") }
                    TextButton(onClick = { pendingDelete = null }) { Text("取消") }
                }
            }
        )
    }

    if (showClearAll) {
        AlertDialog(
            onDismissRequest = { showClearAll = false },
            title = { Text("确认清空，喵~") },
            text = { Text("将清空搜索历史关键字") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllSearchHistory()
                    PopTip.show("已清空了~")
                    showClearAll = false
                }) { Text("确定清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAll = false }) { Text("取消") }
            }
        )
    }
}
