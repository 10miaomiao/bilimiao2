package com.a10miaomiao.bilimiao.comm.entity.filter

/**
 * 屏蔽设置备份的纯文本编解码。
 *
 * 格式：
 * ```
 * # bilimiao filter backup
 * # version: 1.0
 * # exportedAt: ...
 *
 * ## WORDS ##
 * 你好
 *
 * ## UPPERS ##
 * L另唐 uid=31261235
 *
 * ## TAGS ##
 * 兰兰
 * ```
 *
 * 转义规则（导出时一律转义、导入时宽容）：
 * - `\\` → `\`
 * - `\t` → TAB(0x09)
 * - `\#` → `#`（只在行首转义，避免屏蔽词被误认为注释）
 * - `\ ` → 空格（**所有**空格都转义：upper 行用未转义的空格/TAB 分隔字段，
 *   名称里的空格必须转义才不会和分隔符冲突）
 */
object FilterBackupTextFormat {

    private const val FORMAT_VERSION = "1.0"
    private const val HEADER_TITLE = "bilimiao filter backup"

    private const val SECTION_WORDS = "WORDS"
    private const val SECTION_UPPERS = "UPPERS"
    private const val SECTION_TAGS = "TAGS"

    private enum class Section { WORDS, UPPERS, TAGS }

    private val KEY_VALUE_REGEX = Regex("""^[A-Za-z_][A-Za-z0-9_]*=.*$""")

    // ---------------------------------------------------------------- 编码

    fun encode(backup: FilterBackupInfo): String = buildString {
        append("# ").append(HEADER_TITLE).append('\n')
        append("# version: ").append(FORMAT_VERSION).append('\n')
        append("# exportedAt: ").append(backup.exportedAt).append('\n')
        append("# \n")
        append("# 哔哩喵屏蔽关键字备份\n")
        append("# 转义说明：\n")
        append("# `\\\\` -> `\\`, `\\#` ->  `#`, `\\t` -> TAB(0x09), `\\ `(反斜杠+空格) -> 空格\n")
        append("# upper格式为 `name\\tk1=v2\\tk=v`，也可以为`name k1=v1 k2=v2`，uid为可选字段\n")
        append("# upper行用未转义的空格/TAB分隔字段，所以名称里的空格要写成`\\ `\n")
        append('\n')

        append("## ").append(SECTION_WORDS).append(" ##\n")
        backup.filterWords.forEach { append(escapeValue(it)).append('\n') }
        append('\n')

        append("## ").append(SECTION_UPPERS).append(" ##\n")
        backup.filterUppers.forEach { append(escapeUpper(it)).append('\n') }
        append('\n')

        append("## ").append(SECTION_TAGS).append(" ##\n")
        backup.filterTags.forEach { append(escapeValue(it)).append('\n') }
    }

    /**
     * 转义单个值。
     *
     * 空格无论出现在首尾还是中间都转义成 `\ `：upper 行允许用未转义的空格分隔
     * `uid=xxx` 字段，如果名称里的空格不转义，解析时就分不清哪个空格是分隔符、
     * 哪个是名称的一部分。
     */
    private fun escapeValue(value: String): String = buildString {
        value.forEachIndexed { index, ch ->
            when {
                ch == '\\' -> append("\\\\")
                ch == '\t' -> append("\\t")
                ch == '#' && index == 0 -> append("\\#")
                ch == ' ' -> append("\\ ")
                else -> append(ch)
            }
        }
    }

    /**
     * UP 主用真实 TAB 分隔，这样昵称里的空格不会被当成字段分隔符。
     * 只有 uid 没有昵称时也必须保留一个前导 TAB，否则解析端会把 `uid=xxx`
     * 当成昵称（昵称至少要占一段）。
     */
    private fun escapeUpper(upper: FilterBackupUpper): String {
        val name = escapeValue(upper.name)
        val uidPart = upper.uid?.let { "uid=$it" }
        return when {
            uidPart == null -> name
            name.isEmpty() -> "\t$uidPart"
            else -> "$name\t$uidPart"
        }
    }

    // ---------------------------------------------------------------- 解码

    fun decode(text: String): FilterBackupInfo {
        var exportedAt = ""
        val words = mutableListOf<String>()
        val uppers = mutableListOf<FilterBackupUpper>()
        val tags = mutableListOf<String>()
        // 没有分节标记时按屏蔽词处理，方便直接导入一份纯词表
        var section = Section.WORDS

        text.split('\n').forEach { rawLine ->
            val line = rawLine.removeSuffix("\r")
            if (line.isBlank()) {
                return@forEach
            }
            if (line.startsWith("#")) {
                val title = parseSectionTitle(line)
                if (title != null) {
                    section = title
                } else {
                    val meta = parseMeta(line)
                    if (meta != null && meta.first.equals("exportedAt", ignoreCase = true)) {
                        exportedAt = meta.second
                    }
                }
                return@forEach
            }
            when (section) {
                Section.WORDS -> unescape(line, trimUnescapedEdges = true)
                    .takeIf { it.isNotBlank() }
                    ?.let { words.add(it) }

                Section.TAGS -> unescape(line, trimUnescapedEdges = true)
                    .takeIf { it.isNotBlank() }
                    ?.let { tags.add(it) }

                Section.UPPERS -> parseUpper(line)?.let { uppers.add(it) }
            }
        }

        return FilterBackupInfo(
            exportedAt = exportedAt,
            filterWords = words,
            filterUppers = uppers,
            filterTags = tags,
        )
    }

    private fun parseSectionTitle(line: String): Section? {
        if (!line.startsWith("##")) {
            return null
        }
        return when (line.trim('#', ' ', '\t').uppercase()) {
            SECTION_WORDS -> Section.WORDS
            SECTION_UPPERS -> Section.UPPERS
            SECTION_TAGS -> Section.TAGS
            else -> null
        }
    }

    private fun parseMeta(line: String): Pair<String, String>? {
        val body = line.removePrefix("#").trim()
        val index = body.indexOf(':')
        if (index <= 0) {
            return null
        }
        return body.substring(0, index).trim() to body.substring(index + 1).trim()
    }

    /**
     * 解析 `name`、`name\tuid=123`、`name uid=123` 三种写法。
     *
     * 从右往左把 `k=v` 形式的段当作字段，剩下的部分拼回昵称；
     * 但至少保留一段作为昵称，避免把昵称恰好是 `abc=1` 的行当成字段丢弃。
     */
    private fun parseUpper(line: String): FilterBackupUpper? {
        val segments = splitUpperSegments(line)
        if (segments.isEmpty()) {
            return null
        }
        var nameEnd = segments.size
        while (nameEnd > 1 && KEY_VALUE_REGEX.matches(segments[nameEnd - 1])) {
            nameEnd--
        }

        val name = unescape(segments.subList(0, nameEnd).joinToString(" "))
        var uid: Long? = null
        for (index in nameEnd until segments.size) {
            val segment = segments[index]
            val eq = segment.indexOf('=')
            if (eq <= 0) {
                continue
            }
            // 目前只认 uid，其他字段留给以后扩展；
            // mid 是旧版备份文件里的写法，继续兼容读取
            val key = segment.substring(0, eq)
            if (key.equals("uid", ignoreCase = true) || key.equals("mid", ignoreCase = true)) {
                uid = segment.substring(eq + 1).toLongOrNull()
            }
        }
        if (name.isBlank() && uid == null) {
            return null
        }
        return FilterBackupUpper(uid = uid, name = name)
    }

    private fun splitUpperSegments(line: String): List<String> =
        if ('\t' in line) {
            // TAB 分隔时昵称里的空格原样保留，只有真实 TAB 才是分隔符
            line.split('\t').dropLastWhile { it.isEmpty() }
        } else {
            splitByUnescapedSpace(line.trim())
        }

    /** 按未转义的空格拆分，`\ ` 表示字面空格，不当作分隔符 */
    private fun splitByUnescapedSpace(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var index = 0
        while (index < line.length) {
            val ch = line[index]
            when {
                ch == '\\' && index + 1 < line.length -> {
                    current.append(ch).append(line[index + 1])
                    index += 2
                }

                ch == ' ' -> {
                    result.add(current.toString())
                    current.clear()
                    index++
                }

                else -> {
                    current.append(ch)
                    index++
                }
            }
        }
        result.add(current.toString())
        return result.dropLastWhile { it.isEmpty() }
    }

    /**
     * 反转义一行内容。
     *
     * [trimUnescapedEdges] 为 true 时裁掉首尾**未转义**的空白，这是
     * “默认忽略前后空格”的落地方式。注意不能用 String.trim()：
     * `\ ` 的后半部分本身就是一个空格，盲目 trim 会把转义序列拆坏
     * （`广告\ \ ` 会被裁成 `广告\ \`）。
     */
    private fun unescape(text: String, trimUnescapedEdges: Boolean = false): String {
        var index = 0
        if (trimUnescapedEdges) {
            // 遇到反斜杠就停，因为 `\ ` 表示的是需要保留的空格
            while (index < text.length && (text[index] == ' ' || text[index] == '\t')) {
                index++
            }
        }
        val out = StringBuilder(text.length)
        // 记录最后一个有效字符的输出位置，用来裁掉尾部未转义的空白
        var meaningfulEnd = 0
        while (index < text.length) {
            val ch = text[index]
            if (ch == '\\' && index + 1 < text.length) {
                val decoded = when (text[index + 1]) {
                    '\\' -> '\\'
                    '#' -> '#'
                    't' -> '\t'
                    ' ' -> ' '
                    else -> null
                }
                if (decoded != null) {
                    out.append(decoded)
                    index += 2
                    // 来自转义的空白也是有效内容，不能裁掉
                    meaningfulEnd = out.length
                    continue
                }
                // 未知转义原样保留反斜杠，避免误伤正则里的 \d \w 之类
                out.append(ch)
                index++
                meaningfulEnd = out.length
                continue
            }
            out.append(ch)
            index++
            if (!trimUnescapedEdges || (ch != ' ' && ch != '\t')) {
                meaningfulEnd = out.length
            }
        }
        return out.substring(0, meaningfulEnd)
    }
}
