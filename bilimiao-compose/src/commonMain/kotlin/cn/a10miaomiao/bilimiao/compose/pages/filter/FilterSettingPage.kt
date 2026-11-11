package cn.a10miaomiao.bilimiao.compose.pages.filter

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import cn.a10miaomiao.bilimiao.compose.base.ComposePage
import cn.a10miaomiao.bilimiao.compose.common.diViewModel
import cn.a10miaomiao.bilimiao.compose.common.localContentInsets
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageConfig
import cn.a10miaomiao.bilimiao.compose.common.mypage.PageListener
import cn.a10miaomiao.bilimiao.compose.common.platform.rememberTextFilePicker
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterTagListContent
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterUpperListContent
import cn.a10miaomiao.bilimiao.compose.pages.filter.content.FilterWordListContent
import com.a10miaomiao.bilimiao.comm.db.FilterImportPreview
import com.a10miaomiao.bilimiao.comm.db.FilterMergeResult
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupInfo
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupSelection
import com.a10miaomiao.bilimiao.comm.mypage.myMenu
import com.a10miaomiao.bilimiao.comm.store.FilterStore
import com.a10miaomiao.bilimiao.comm.toast.GlobalToaster
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import kotlin.time.Clock

/** 右上角菜单项，key 在页面内唯一即可 */
private object MenuKeys {
    const val importBackup = 1
    const val exportBackup = 2
}

@Serializable
class FilterSettingPage : ComposePage {

    @Composable
    override fun Content() {
        val viewModel: FilterSettingPageViewModel = diViewModel {
            FilterSettingPageViewModel(it)
        }
        FilterSettingPageContent(viewModel)
    }

}

private class FilterSettingPageViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    private val filterStore by instance<FilterStore>()

    val stateFlow get() = filterStore.stateFlow

    fun buildBackup(selection: FilterBackupSelection) = filterStore.buildBackup(selection)

    suspend fun previewImport(
        backup: FilterBackupInfo,
        selection: FilterBackupSelection,
    ): FilterImportPreview = filterStore.previewImport(backup, selection)

    fun importBackup(
        backup: FilterBackupInfo,
        selection: FilterBackupSelection,
    ): FilterMergeResult = filterStore.importBackup(backup, selection)

    /** 导出文件名：哔哩喵屏蔽备份-2026-11-11.txt */
    fun buildBackupFileName(): String {
        val date = Clock.System.now()
            .toLocalDateTime(TimeZone.currentSystemDefault())
            .date
        return "哔哩喵屏蔽备份-$date.txt"
    }
}

@Composable
private fun CategoryCheckRow(
    label: String,
    count: Int,
    unit: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = count > 0,
        )
        Text(text = "$label（$count $unit）")
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun FilterSettingPageContent(
    viewModel: FilterSettingPageViewModel
) {
    val scope = rememberCoroutineScope()
    val filePicker = rememberTextFilePicker()
    val state by viewModel.stateFlow.collectAsState()

    val wordCount = state.filterWordList.size
    val upperCount = state.filterUpperList.size
    val tagCount = state.filterTagList.size

    // 导出对话框及其勾选状态
    var showExportDialog by remember {
        mutableStateOf(false)
    }
    var exportWords by remember {
        mutableStateOf(true)
    }
    var exportUppers by remember {
        mutableStateOf(true)
    }
    var exportTags by remember {
        mutableStateOf(true)
    }

    // 非空时展示导入确认对话框
    var pendingBackup by remember {
        mutableStateOf<FilterBackupInfo?>(null)
    }

    fun handleExportClick() {
        // 每次打开都重置为全选
        exportWords = true
        exportUppers = true
        exportTags = true
        showExportDialog = true
    }

    fun handleImportClick() {
        filePicker.openText { text ->
            if (text == null) {
                GlobalToaster.show("导入已取消")
                return@openText
            }
            val backup = runCatching { FilterBackupInfo.parseText(text) }.getOrNull()
            if (backup == null || backup.totalCount == 0) {
                GlobalToaster.show("不是有效的屏蔽设置备份文件")
                return@openText
            }
            // 先让用户选择导入范围并预览结果，避免误选文件直接写入
            pendingBackup = backup
        }
    }

    val pageConfigId = PageConfig(
        title = "屏蔽设置",
        menu = remember {
            myMenu {
                myItem {
                    key = MenuKeys.importBackup
                    title = "导入备份"
                    iconVector = Icons.Default.FileDownload
                }
                myItem {
                    key = MenuKeys.exportBackup
                    title = "导出备份"
                    iconVector = Icons.Default.FileUpload
                }
            }
        }
    )
    PageListener(
        configId = pageConfigId,
        onMenuItemClick = { menuItem ->
            when (menuItem.key) {
                MenuKeys.importBackup -> handleImportClick()
                MenuKeys.exportBackup -> handleExportClick()
            }
        }
    )

    val windowInsets = localContentInsets()
    val pagerState = rememberPagerState(pageCount = { 3 })
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        PrimaryTabRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(
                    top = windowInsets.topDp.dp,
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                ),
            selectedTabIndex = pagerState.currentPage,
        ) {
            Tab(
                text = {
                    Text(
                        text = "屏蔽关键字",
                        color = if (0 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 0,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
            )

            Tab(
                text = {
                    Text(
                        text = "屏蔽UP主",
                        color = if (1 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 1,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
            )

            Tab(
                text = {
                    Text(
                        text = "屏蔽标签",
                        color = if (2 == pagerState.currentPage) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        }
                    )
                },
                selected = pagerState.currentPage == 2,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                },
            )
        }
        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = windowInsets.leftDp.dp,
                    end = windowInsets.rightDp.dp,
                )
                .weight(1f),
            state = pagerState,
        ) { index ->
            when(index) {
                0 -> {
                    FilterWordListContent()
                }
                1 -> {
                    FilterUpperListContent()
                }
                2 -> {
                    FilterTagListContent()
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(windowInsets.bottom),
            contentAlignment = Alignment.Center
        ) {
            Text(text =  "可进【设置】->【帮助】查看使用方法")
        }
        Spacer(modifier = Modifier.height(windowInsets.bottom))
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
            },
            title = {
                Text(text = "导出屏蔽备份")
            },
            text = {
                Column {
                    Text(text = "选择要导出的内容：")
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryCheckRow(
                        label = "屏蔽关键字",
                        count = wordCount,
                        unit = "条",
                        checked = exportWords,
                        onCheckedChange = { exportWords = it },
                    )
                    CategoryCheckRow(
                        label = "屏蔽UP主",
                        count = upperCount,
                        unit = "个",
                        checked = exportUppers,
                        onCheckedChange = { exportUppers = it },
                    )
                    CategoryCheckRow(
                        label = "屏蔽标签",
                        count = tagCount,
                        unit = "个",
                        checked = exportTags,
                        onCheckedChange = { exportTags = it },
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = exportWords || exportUppers || exportTags,
                    onClick = {
                        val selection = FilterBackupSelection(
                            includeWords = exportWords,
                            includeUppers = exportUppers,
                            includeTags = exportTags,
                        )
                        val backup = viewModel.buildBackup(selection)
                        showExportDialog = false
                        if (backup.totalCount == 0) {
                            GlobalToaster.show("所选类别没有内容")
                        } else {
                            filePicker.saveText(
                                suggestedName = viewModel.buildBackupFileName(),
                                content = backup.toText(),
                            ) { success ->
                                if (success) {
                                    GlobalToaster.show("已导出 ${backup.totalCount} 条屏蔽设置")
                                } else {
                                    GlobalToaster.show("导出已取消")
                                }
                            }
                        }
                    }
                ) {
                    Text(text = "导出")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showExportDialog = false
                }) {
                    Text(text = "取消")
                }
            }
        )
    }

    val backup = pendingBackup
    if (backup != null) {
        var importWords by remember(backup) {
            mutableStateOf(backup.filterWords.isNotEmpty())
        }
        var importUppers by remember(backup) {
            mutableStateOf(backup.filterUppers.isNotEmpty())
        }
        var importTags by remember(backup) {
            mutableStateOf(backup.filterTags.isNotEmpty())
        }
        val selection = FilterBackupSelection(
            includeWords = importWords,
            includeUppers = importUppers,
            includeTags = importTags,
        )
        var preview by remember(backup) {
            mutableStateOf<FilterImportPreview?>(null)
        }
        // 勾选变化时重新预演，统计随选择实时更新
        LaunchedEffect(backup, selection) {
            preview = if (selection.isEmpty) {
                FilterImportPreview()
            } else {
                viewModel.previewImport(backup, selection)
            }
        }

        AlertDialog(
            onDismissRequest = {
                pendingBackup = null
            },
            title = {
                Text(text = "导入屏蔽备份")
            },
            text = {
                Column {
                    Text(text = "选择要导入的内容：")
                    Spacer(modifier = Modifier.height(8.dp))
                    CategoryCheckRow(
                        label = "屏蔽关键字",
                        count = backup.filterWords.size,
                        unit = "条",
                        checked = importWords,
                        onCheckedChange = { importWords = it },
                    )
                    CategoryCheckRow(
                        label = "屏蔽UP主",
                        count = backup.filterUppers.size,
                        unit = "个",
                        checked = importUppers,
                        onCheckedChange = { importUppers = it },
                    )
                    CategoryCheckRow(
                        label = "屏蔽标签",
                        count = backup.filterTags.size,
                        unit = "个",
                        checked = importTags,
                        onCheckedChange = { importTags = it },
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = preview?.summary() ?: "正在统计…")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "导入会与当前设置合并，相同内容不会重复添加。",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    enabled = (preview?.importable ?: 0) > 0,
                    onClick = {
                        val current = preview
                        if (current != null) {
                            pendingBackup = null
                            val result = viewModel.importBackup(backup, selection)
                            GlobalToaster.show(
                                if (result.total > 0 || result.skippedUppers > 0) {
                                    result.summary()
                                } else {
                                    "没有需要新增的屏蔽设置"
                                }
                            )
                        }
                    }
                ) {
                    Text(text = "导入")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingBackup = null
                }) {
                    Text(text = "取消")
                }
            }
        )
    }
}
