package com.a10miaomiao.bilimiao.comm.entity.filter

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FilterBackupTextFormatTest {

    @Test
    fun parsesSampleFileFromSpec() {
        val text = """
            # bilimiao filter backup
            # version: 1.0
            # exportedAt: 2026-11-11T13:08:05.738124900Z
            # 
            # 哔哩喵屏蔽关键字备份
            # 转义说明：
            # `\\` -> `\`, `\#` ->  `#`, `\t` -> TAB(0x09), `\ `(反斜杠+空格) -> 空格
            # upper格式为 `name\tk1=v2\tk=v`，也可以为`name k1=v1 k2=v2`，mid为可选字段
            # upper行用未转义的空格/TAB分隔字段，所以名称里的空格要写成`\ `

            ## WORDS ##
            你好
            哈哈哈

            ## UPPERS ##
            L另唐 mid=31261235

            ## TAGS ##
            兰兰
        """.trimIndent()

        val backup = FilterBackupInfo.parseText(text)

        assertEquals("2026-11-11T13:08:05.738124900Z", backup.exportedAt)
        assertEquals(listOf("你好", "哈哈哈"), backup.filterWords)
        assertEquals(listOf(FilterBackupUpper(31261235L, "L另唐")), backup.filterUppers)
        assertEquals(listOf("兰兰"), backup.filterTags)
    }

    @Test
    fun escapesEverySpaceSoUpperFieldsStayUnambiguous() {
        // upper 行允许用未转义空格分隔 mid=xxx，所以名称里的空格（含中间的）必须转义，
        // 否则解析时分不清哪个空格是字段分隔符
        val backup = FilterBackupInfo(
            filterWords = listOf("你好 世界"),
            filterUppers = listOf(FilterBackupUpper(42L, "老王 视频")),
        )

        val text = backup.toText()
        assertTrue("屏蔽词中间的空格也要转义", text.contains("你好\\ 世界"))
        assertTrue("UP 主昵称中间的空格也要转义", text.contains("老王\\ 视频"))

        val restored = FilterBackupInfo.parseText(text)
        assertEquals(listOf("你好 世界"), restored.filterWords)
        assertEquals("老王 视频", restored.filterUppers.single().name)
    }

    @Test
    fun upperWithOnlyMidKeepsItAsMid() {
        // 没有昵称时必须保留前导 TAB，否则 mid=xxx 会被当成昵称
        val backup = FilterBackupInfo(filterUppers = listOf(FilterBackupUpper(42L, "")))

        val upper = FilterBackupInfo.parseText(backup.toText()).filterUppers.single()
        assertEquals(42L, upper.mid)
        assertEquals("", upper.name)
    }

    @Test
    fun roundTripKeepsEveryValue() {
        val original = FilterBackupInfo(
            exportedAt = "2026-01-01T00:00:00Z",
            filterWords = listOf("广告", "/推广|恰饭/", " 前后空格 ", "#话题", "反\\斜杠", "带\t制表符"),
            filterUppers = listOf(
                FilterBackupUpper(1234567890123456789L, "某UP主"),
                FilterBackupUpper(42L, "名字 带空格 的UP"),
            ),
            filterTags = listOf("兰兰", "单机游戏"),
        )

        val restored = FilterBackupInfo.parseText(original.toText())

        assertEquals(original, restored)
    }

    @Test
    fun ignoresLeadingAndTrailingSpacesByDefault() {
        val backup = FilterBackupInfo(filterWords = listOf("  广告  "))

        val text = backup.toText()
        // 首尾空格必须转义，否则往返后就丢了
        assertTrue("首尾空格应当被转义", text.contains("\\ "))

        assertEquals(listOf("  广告  "), FilterBackupInfo.parseText(text).filterWords)
    }

    @Test
    fun escapesLineLeadingHashSoItIsNotTreatedAsComment() {
        val backup = FilterBackupInfo(filterWords = listOf("#每天一个冷知识"))

        val text = backup.toText()
        assertTrue("行首 # 应当转义", text.contains("\\#每天一个冷知识"))

        assertEquals(listOf("#每天一个冷知识"), FilterBackupInfo.parseText(text).filterWords)
    }

    @Test
    fun keepsMidExactly() {
        val mid = 1234567890123456789L
        val backup = FilterBackupInfo(filterUppers = listOf(FilterBackupUpper(mid, "某UP主")))

        val text = backup.toText()
        assertTrue("mid 必须以十进制原样写出", text.contains("mid=$mid"))

        assertEquals(mid, FilterBackupInfo.parseText(text).filterUppers.single().mid)
    }

    @Test
    fun supportsTabSeparatedUpper() {
        // name 里的空格不需要转义，只有真实 TAB 才是分隔符
        val text = """
            ## UPPERS ##
            名字 带空格 的UP	mid=42
        """.trimIndent()

        val upper = FilterBackupInfo.parseText(text).filterUppers.single()
        assertEquals(42L, upper.mid)
        assertEquals("名字 带空格 的UP", upper.name)
    }

    @Test
    fun supportsSpaceSeparatedUpperWithEscapedSpaceInName() {
        val text = """
            ## UPPERS ##
            名字\ 带空格 mid=42
        """.trimIndent()

        val upper = FilterBackupInfo.parseText(text).filterUppers.single()
        assertEquals(42L, upper.mid)
        assertEquals("名字 带空格", upper.name)
    }

    @Test
    fun keepsUpperWithoutMidAsItsName() {
        // 昵称恰好长得像 k=v 时不能被当成字段吃掉
        val text = """
            ## UPPERS ##
            a=b
        """.trimIndent()

        val upper = FilterBackupInfo.parseText(text).filterUppers.single()
        assertEquals("a=b", upper.name)
        assertNull(upper.mid)
    }

    @Test
    fun defaultsToWordsWhenSectionHeaderIsMissing() {
        // 允许直接导入一份纯词表
        val backup = FilterBackupInfo.parseText("广告\n推广\n\n恰饭")

        assertEquals(listOf("广告", "推广", "恰饭"), backup.filterWords)
        assertTrue(backup.filterUppers.isEmpty())
        assertTrue(backup.filterTags.isEmpty())
    }

    @Test
    fun keepsUnknownEscapesForRegex() {
        // 正则里的 \d \w 不能被当成无效转义吃掉反斜杠
        val backup = FilterBackupInfo(filterWords = listOf("/^\\d{4}-\\w+$/"))

        val restored = FilterBackupInfo.parseText(backup.toText()).filterWords
        assertEquals(listOf("/^\\d{4}-\\w+$/"), restored)
    }

    @Test
    fun ignoresCommentsAndBlankLines() {
        val text = """
            # 随便写的注释
            ## WORDS ##
            广告

            # 中间插一行注释
            推广
        """.trimIndent()

        assertEquals(listOf("广告", "推广"), FilterBackupInfo.parseText(text).filterWords)
    }

    @Test
    fun exportedTextIsHumanReadable() {
        val text = FilterBackupInfo(
            exportedAt = "2026-01-01T00:00:00Z",
            filterWords = listOf("广告"),
        ).toText()

        assertTrue("应当带格式说明", text.contains("哔哩喵屏蔽关键字备份"))
        assertTrue("应当带分节标记", text.contains("## WORDS ##"))
        assertTrue("应当带分节标记", text.contains("## UPPERS ##"))
        assertTrue("应当带分节标记", text.contains("## TAGS ##"))
        // 一行一条，不夹杂引号和逗号
        val wordLines = text.lines().filter { it == "广告" }
        assertEquals(1, wordLines.size)
    }
}
