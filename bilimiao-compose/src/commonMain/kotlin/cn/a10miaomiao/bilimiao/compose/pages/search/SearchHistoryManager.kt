package cn.a10miaomiao.bilimiao.compose.pages.search

interface SearchHistoryManager {
    suspend fun queryAllHistory(): List<String>
    suspend fun insertHistory(keyword: String)
    suspend fun deleteHistory(keyword: String)
    suspend fun deleteAllHistory()
}

expect fun createSearchHistoryManager(): SearchHistoryManager
