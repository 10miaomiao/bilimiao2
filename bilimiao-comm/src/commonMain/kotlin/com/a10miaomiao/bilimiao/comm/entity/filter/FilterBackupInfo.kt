package com.a10miaomiao.bilimiao.comm.entity.filter

/**
 * 屏蔽设置备份数据，文本格式见 [FilterBackupTextFormat]。
 *
 * 没有用 JSON：屏蔽词支持 /正则/ 语法，会带 & < > 等字符，
 * 而这份备份的主要用途就是人工查看、手工编辑和在用户之间分享，
 * 一行一条的纯文本比带引号和逗号的 JSON 好改也好复制。
 *
 * @param exportedAt 导出时间（ISO-8601），仅供人阅读，不参与解析逻辑
 */
data class FilterBackupInfo(
    val exportedAt: String = "",
    val filterWords: List<String> = emptyList(),
    val filterUppers: List<FilterBackupUpper> = emptyList(),
    val filterTags: List<String> = emptyList(),
) {

    val totalCount get() = filterWords.size + filterUppers.size + filterTags.size

    fun toText(): String = FilterBackupTextFormat.encode(this)

    /** 按勾选的类别裁剪，用于“只导出其中几类” */
    fun selected(selection: FilterBackupSelection): FilterBackupInfo = FilterBackupInfo(
        exportedAt = exportedAt,
        filterWords = if (selection.includeWords) filterWords else emptyList(),
        filterUppers = if (selection.includeUppers) filterUppers else emptyList(),
        filterTags = if (selection.includeTags) filterTags else emptyList(),
    )

    companion object {
        fun parseText(text: String): FilterBackupInfo = FilterBackupTextFormat.decode(text)
    }
}

/**
 * 导出/导入时勾选的类别
 */
data class FilterBackupSelection(
    val includeWords: Boolean = true,
    val includeUppers: Boolean = true,
    val includeTags: Boolean = true,
) {
    val isEmpty get() = !includeWords && !includeUppers && !includeTags

    companion object {
        val All = FilterBackupSelection()
    }
}

/**
 * @param mid 屏蔽依据的 UID。备份文件里允许省略，此时为 null，
 *            合并时会跳过（当前版本只按 UID 屏蔽，昵称屏蔽还没实现）
 */
data class FilterBackupUpper(
    val mid: Long? = null,
    val name: String = "",
)
