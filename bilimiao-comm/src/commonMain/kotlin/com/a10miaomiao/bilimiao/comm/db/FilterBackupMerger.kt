package com.a10miaomiao.bilimiao.comm.db

import com.a10miaomiao.bilimiao.comm.db.entity.FilterTagEntity
import com.a10miaomiao.bilimiao.comm.db.entity.FilterUpperEntity
import com.a10miaomiao.bilimiao.comm.db.entity.FilterWordEntity
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupInfo
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupSelection

/**
 * 导入前的预演结果，用来在确认对话框里告诉用户会发生什么。
 *
 * 恒等式：total = importable + duplicated + existCount
 * （缺少 mid 的 UP 主不计入 total，单独用 [skippedUppers] 统计）
 */
data class FilterImportPreview(
    val total: Int = 0,
    val importable: Int = 0,
    val duplicated: Int = 0,
    val existCount: Int = 0,
    val skippedUppers: Int = 0,
) {
    /** 为 0 的项不显示 */
    fun summary(): String {
        val parts = mutableListOf<String>()
        if (total > 0) parts.add("共 $total 条")
        if (importable > 0) parts.add("可导入 $importable 条")
        if (duplicated > 0) parts.add("$duplicated 条重复")
        if (existCount > 0) parts.add("$existCount 条已存在")
        if (skippedUppers > 0) parts.add("$skippedUppers 条缺少 mid 无法导入")
        return if (parts.isEmpty()) "没有可导入的内容" else parts.joinToString("，")
    }
}

/**
 * 合并结果，各字段是实际新增的条数（数据库中已存在的不计入）
 */
data class FilterMergeResult(
    val addedWords: Int = 0,
    val addedTags: Int = 0,
    val addedUppers: Int = 0,
    val skippedUppers: Int = 0,
) {
    val total get() = addedWords + addedTags + addedUppers

    fun summary(): String = buildString {
        append("新增 $addedWords 个屏蔽词、$addedUppers 个 UP 主、$addedTags 个标签")
        if (skippedUppers > 0) {
            append("；跳过 $skippedUppers 个缺少 mid 的 UP 主")
        }
    }
}

/**
 * 把备份数据按内容取并集写入数据库。导入功能和旧版数据库迁移共用这段逻辑。
 *
 * 注意不能依赖 `INSERT OR REPLACE` 去重：filter_world / filter_tag 的主键是自增 id，
 * keyword / name 没有唯一约束，REPLACE 会按 id 冲突覆盖掉新库里用户后加的记录。
 * 因此必须先把已有内容查出来再过滤，这样同一份备份重复导入也不会产生重复数据。
 */
internal suspend fun mergeFilterBackup(
    db: FilterDatabase,
    backup: FilterBackupInfo,
    selection: FilterBackupSelection = FilterBackupSelection.All,
): FilterMergeResult {
    val plan = buildMergePlan(db, backup, selection)
    val wordDao = db.filterWordDao()
    val tagDao = db.filterTagDao()
    val upperDao = db.filterUpperDao()

    plan.words.forEach { word ->
        wordDao.insert(FilterWordEntity(keyword = word))
    }
    plan.tags.forEach { name ->
        tagDao.insert(FilterTagEntity(name = name))
    }
    plan.uppers.forEach { entity ->
        upperDao.insert(entity)
    }

    return FilterMergeResult(
        addedWords = plan.words.size,
        addedTags = plan.tags.size,
        addedUppers = plan.uppers.size,
        skippedUppers = plan.skippedUppers,
    )
}

/**
 * 只计算不落库，供导入确认对话框展示
 */
internal suspend fun previewFilterBackup(
    db: FilterDatabase,
    backup: FilterBackupInfo,
    selection: FilterBackupSelection,
): FilterImportPreview {
    val plan = buildMergePlan(db, backup, selection)
    return FilterImportPreview(
        total = plan.words.size + plan.uppers.size + plan.tags.size + plan.duplicated + plan.existCount,
        importable = plan.words.size + plan.uppers.size + plan.tags.size,
        duplicated = plan.duplicated,
        existCount = plan.existCount,
        skippedUppers = plan.skippedUppers,
    )
}

/**
 * 一次扫描得出“要插入哪些”和各维度计数。
 * 预览和真正的合并都走这里，避免两处统计口径不一致。
 */
private class MergePlan(
    val words: List<String>,
    val tags: List<String>,
    val uppers: List<FilterUpperEntity>,
    val duplicated: Int,
    val existCount: Int,
    val skippedUppers: Int,
)

private suspend fun buildMergePlan(
    db: FilterDatabase,
    backup: FilterBackupInfo,
    selection: FilterBackupSelection,
): MergePlan {
    var duplicated = 0
    var existCount = 0
    var skippedUppers = 0

    val words = mutableListOf<String>()
    if (selection.includeWords) {
        val existWords = db.filterWordDao().queryAll().mapTo(HashSet()) { it.keyword }
        val seen = HashSet<String>()
        // HashSet.add 返回 false 说明同一份文件里出现了两次，算“重复”而不是“已存在”
        backup.filterWords.forEach { word ->
            when {
                word.isBlank() -> Unit
                !seen.add(word) -> duplicated++
                word in existWords -> existCount++
                else -> words.add(word)
            }
        }
    }

    val tags = mutableListOf<String>()
    if (selection.includeTags) {
        val existTags = db.filterTagDao().queryAll().mapTo(HashSet()) { it.name }
        val seen = HashSet<String>()
        backup.filterTags.forEach { name ->
            when {
                name.isBlank() -> Unit
                !seen.add(name) -> duplicated++
                name in existTags -> existCount++
                else -> tags.add(name)
            }
        }
    }

    val uppers = mutableListOf<FilterUpperEntity>()
    if (selection.includeUppers) {
        val existUppers = db.filterUpperDao().queryAll().mapTo(HashSet()) { it.mid }
        val seen = HashSet<Long>()
        backup.filterUppers.forEach { upper ->
            val mid = upper.mid
            when {
                // 没有 mid 就没法按 UID 屏蔽，昵称屏蔽还是 TODO
                mid == null -> skippedUppers++
                !seen.add(mid) -> duplicated++
                mid in existUppers -> existCount++
                else -> uppers.add(FilterUpperEntity(mid = mid, name = upper.name))
            }
        }
    }

    return MergePlan(
        words = words,
        tags = tags,
        uppers = uppers,
        duplicated = duplicated,
        existCount = existCount,
        skippedUppers = skippedUppers,
    )
}
