package cn.a10miaomiao.bilimiao.compose.components.start

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import cn.a10miaomiao.bilimiao.compose.base.PageSearchMethod
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.navigation.PageNavigation
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoCard
import cn.a10miaomiao.bilimiao.compose.components.miao.MiaoOutlinedCard
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchInputViewModel.SuggestInfo
import cn.a10miaomiao.bilimiao.compose.pages.search.SearchResultPage
import com.kongzue.dialogx.dialogs.PopTip
import kotlinx.coroutines.delay
import org.kodein.di.compose.rememberInstance

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchInputInline(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    initKeyword: String,
    initMode: Int,
    pageSearchMethod: PageSearchMethod?,
    onDismissRequest: () -> Unit,
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT

    val viewModel: SearchInputViewModel = diViewModel()
    val pageNavigation: PageNavigation by rememberInstance()
    val activity: Activity by rememberInstance()

    var text by remember { mutableStateOf(initKeyword) }
    var mode by remember { mutableStateOf(initMode) }
    val focusRequester = remember { FocusRequester() }
    var isEditingHistory by remember { mutableStateOf(false) }

    LaunchedEffect(text) {
        viewModel.loadSuggestData(text, text)
        if (text.isNotEmpty()) {
            isEditingHistory = false
        }
    }

    BackHandler {
        onDismissRequest()
    }

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
            pageSearchMethod?.onSearch(keyword)
        }
        onDismissRequest()
    }

    fun deleteHistory(text: String) {
        viewModel.deleteSearchHistory(text)
        PopTip.show("已删除")
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .let {
                if (isCompact) {
                    it.imePadding()
                } else {
                    it
                        .safeDrawingPadding()
                        .padding(8.dp)
                }
            }
            .then(modifier),
        contentAlignment = if (isCompact) Alignment.BottomCenter
            else Alignment.TopStart,
    ) {
        MiaoOutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .run {
                    with(sharedTransitionScope) {
                        sharedElement(
                            rememberSharedContentState(key = "search"),
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                },
            enabled = false,
        ) {
            val historySuggestList by viewModel.historyListFlow.collectAsState()
            val suggestList by viewModel.suggestListFlow.collectAsState()
            val scrollState = rememberScrollState()
            LaunchedEffect(historySuggestList) {
                if (historySuggestList.isEmpty()) {
                    isEditingHistory = false
                }
            }
            val showSuggestList by remember {
                derivedStateOf {
                    when {
                        text.isEmpty() -> historySuggestList
                        else -> suggestList
                    }.let {
                        if (isCompact) it.asReversed() else it
                    }
                }
            }
            LaunchedEffect(showSuggestList) {
                if (isEditingHistory) {
                    return@LaunchedEffect
                }
                if (isCompact && showSuggestList.isNotEmpty()) {
                    scrollState.animateScrollTo(scrollState.maxValue)
                } else {
                    scrollState.scrollTo(0)
                }
            }

            if (!isCompact) {
                SearchTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    isCompact = false,
                    text = text,
                    onTextChange = { text = it },
                    onSearch = ::startSearch,
                    focusRequester = focusRequester,
                    mode = mode,
                    onModeChange = { mode = it },
                    pageSearchMethod = pageSearchMethod,
                )
            }

            if (text.isEmpty() && showSuggestList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "搜索历史",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (historySuggestList.isNotEmpty()) {
                        val contentPadding = PaddingValues(0.dp)
                        if (isEditingHistory) {
                            TextButton(
                                onClick = { showClearAll = true },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("清空")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = {
                                    isEditingHistory = false
                                },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("完成")
                            }
                        } else {
                            TextButton(
                                onClick = { isEditingHistory = true },
                                modifier = Modifier.align(Alignment.CenterVertically)
                                    .height(32.dp),
                                contentPadding = contentPadding,
                            ) {
                                Text("编辑")
                            }
                        }
                    }
                }
            }

            // Suggestions list displayed as flow chips
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .verticalScroll(scrollState),
                contentAlignment = Alignment.BottomStart
            ) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    showSuggestList.forEach { item: SuggestInfo ->
                        val isHistoryItem = item.type == SearchInputViewModel.SuggestType.HISTORY
                        if (text.isEmpty() && isHistoryItem && isEditingHistory) {
                            SuggestionChip(
                                onClick = {
                                    deleteHistory(item.text)
                                },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(item.text)
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "删除搜索历史",
                                            modifier = Modifier.size(12.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            )
                        } else {
                            key(item.text) {
                                SuggestionChip(
                                    onClick = {
                                        when (item.type) {
                                            SearchInputViewModel.SuggestType.AV -> {
                                                pageNavigation.navigateByUri(Uri.parse("bilimiao://video/${item.value}"))
                                            }
                                            SearchInputViewModel.SuggestType.SS -> {
                                                pageNavigation.navigateByUri(Uri.parse("bilimiao://video/${item.value}"))
                                            }
                                            else -> {
                                                startSearch(item.value)
                                            }
                                        }
                                        onDismissRequest()
                                    },
                                    label = { Text(item.text) }
                                )
                            }
                        }
                    }
                }
            }
            if (isCompact) {
                SearchTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    isCompact = true,
                    text = text,
                    onTextChange = { text = it },
                    onSearch = ::startSearch,
                    focusRequester = focusRequester,
                    mode = mode,
                    onModeChange = { mode = it },
                    pageSearchMethod = pageSearchMethod,
                )
            }
        }
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
                    isEditingHistory = false
                    showClearAll = false
                }) { Text("确定清空") }
            },
            dismissButton = {
                TextButton(onClick = { showClearAll = false }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun SearchTextField(
    modifier: Modifier = Modifier,
    isCompact: Boolean,
    text: String,
    onTextChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    focusRequester: FocusRequester,
    mode: Int,
    onModeChange: (Int) -> Unit,
    pageSearchMethod: PageSearchMethod?,
) {
    // Bottom input and actions bar
    Surface(
        modifier = modifier,
        tonalElevation = 3.dp,
        shadowElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            if (isCompact) {
                SearchModeSelector(
                    mode = mode,
                    onModeChange = onModeChange,
                    pageSearchMethod = pageSearchMethod,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
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
                    onValueChange = onTextChange,
                    singleLine = true,
                    placeholder = { Text("输入ID或关键字") },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (text.isNotEmpty()) {
                                IconButton(
                                    modifier = Modifier.size(24.dp),
                                    onClick = { onTextChange("") },
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentDescription = "清空"
                                    )
                                }
                            }
                            TextButton(
                                onClick = { onSearch(text) },
                                enabled = text.isNotEmpty()
                            ) {
                                Text("搜索")
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onSearch(text) }
                    ),
                    shape = MaterialTheme.shapes.large,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            if (!isCompact) {
                SearchModeSelector(
                    mode = mode,
                    onModeChange = onModeChange,
                    pageSearchMethod = pageSearchMethod,
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun SearchModeSelector(
    mode: Int,
    onModeChange: (Int) -> Unit,
    pageSearchMethod: PageSearchMethod?,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = mode == 0,
            onClick = { onModeChange(0) },
            label = { Text("全站搜索") }
        )
        pageSearchMethod?.let {
            FilterChip(
                selected = mode == 1,
                onClick = { onModeChange(1) },
                label = { Text(it.name) }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
    }
}
