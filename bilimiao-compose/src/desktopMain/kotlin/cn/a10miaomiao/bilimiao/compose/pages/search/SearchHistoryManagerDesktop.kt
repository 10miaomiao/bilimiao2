package cn.a10miaomiao.bilimiao.compose.pages.search

actual fun createSearchHistoryManager(): SearchHistoryManager {
    return DesktopSearchHistoryManager()
}

private class DesktopSearchHistoryManager : SearchHistoryManager {
    private val history = mutableListOf<String>()

    override suspend fun queryAllHistory(): List<String> {
        return history.toList()
    }

    override suspend fun insertHistory(keyword: String) {
        history.remove(keyword)
        history.add(0, keyword)
    }

    override suspend fun deleteHistory(keyword: String) {
        history.remove(keyword)
    }

    override suspend fun deleteAllHistory() {
        history.clear()
    }
}
