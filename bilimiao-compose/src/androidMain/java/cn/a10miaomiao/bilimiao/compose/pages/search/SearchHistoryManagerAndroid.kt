package cn.a10miaomiao.bilimiao.compose.pages.search

import com.a10miaomiao.bilimiao.comm.db.createSearchHistoryDatabase
import com.a10miaomiao.bilimiao.comm.db.entity.SearchHistoryEntity

actual fun createSearchHistoryManager(): SearchHistoryManager {
    val db = createSearchHistoryDatabase()
    val dao = db.searchHistoryDao()
    return object : SearchHistoryManager {
        override suspend fun queryAllHistory(): List<String> {
            return dao.queryAllHistory().map { it.keyword }
        }

        override suspend fun insertHistory(keyword: String) {
            dao.insertHistory(SearchHistoryEntity(keyword = keyword))
        }

        override suspend fun deleteHistory(keyword: String) {
            dao.deleteHistory(keyword)
        }

        override suspend fun deleteAllHistory() {
            dao.deleteAllHistory()
        }
    }
}
