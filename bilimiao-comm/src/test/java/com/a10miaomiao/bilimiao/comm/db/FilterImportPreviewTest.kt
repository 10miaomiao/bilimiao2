package com.a10miaomiao.bilimiao.comm.db

import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupInfo
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupSelection
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupUpper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterImportPreviewTest {

    @Test
    fun summaryShowsAllNonZeroParts() {
        val preview = FilterImportPreview(
            total = 20,
            importable = 15,
            duplicated = 3,
            existCount = 2,
        )
        assertEquals("共 20 条，可导入 15 条，3 条重复，2 条已存在", preview.summary())
    }

    @Test
    fun summaryHidesZeroParts() {
        val preview = FilterImportPreview(
            total = 10,
            importable = 10,
            duplicated = 0,
            existCount = 0,
        )
        val text = preview.summary()
        assertEquals("共 10 条，可导入 10 条", text)
        assertTrue("为 0 的项不该出现", "重复" !in text)
        assertTrue("为 0 的项不该出现", "已存在" !in text)
    }

    @Test
    fun summaryWhenNothingSelectable() {
        assertEquals("没有可导入的内容", FilterImportPreview().summary())
    }

    @Test
    fun summaryMentionsUppersWithoutMid() {
        val preview = FilterImportPreview(
            total = 5,
            importable = 5,
            skippedUppers = 2,
        )
        assertEquals("共 5 条，可导入 5 条，2 条缺少 mid 无法导入", preview.summary())
    }

    @Test
    fun selectedKeepsOnlyCheckedCategories() {
        val backup = FilterBackupInfo(
            exportedAt = "2026-01-01T00:00:00Z",
            filterWords = listOf("广告"),
            filterUppers = listOf(FilterBackupUpper(42L, "某UP主")),
            filterTags = listOf("兰兰"),
        )

        val onlyWords = backup.selected(
            FilterBackupSelection(includeWords = true, includeUppers = false, includeTags = false)
        )
        assertEquals(listOf("广告"), onlyWords.filterWords)
        assertTrue(onlyWords.filterUppers.isEmpty())
        assertTrue(onlyWords.filterTags.isEmpty())
        assertEquals("导出时间要保留", "2026-01-01T00:00:00Z", onlyWords.exportedAt)

        val onlyUppersAndTags = backup.selected(
            FilterBackupSelection(includeWords = false, includeUppers = true, includeTags = true)
        )
        assertTrue(onlyUppersAndTags.filterWords.isEmpty())
        assertEquals(1, onlyUppersAndTags.filterUppers.size)
        assertEquals(1, onlyUppersAndTags.filterTags.size)
    }

    @Test
    fun emptySelectionIsDetected() {
        assertTrue(
            FilterBackupSelection(
                includeWords = false,
                includeUppers = false,
                includeTags = false,
            ).isEmpty
        )
        assertTrue(!FilterBackupSelection.All.isEmpty)
    }
}
