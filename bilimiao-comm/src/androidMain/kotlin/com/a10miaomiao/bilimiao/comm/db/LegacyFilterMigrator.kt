package com.a10miaomiao.bilimiao.comm.db

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupInfo
import com.a10miaomiao.bilimiao.comm.entity.filter.FilterBackupUpper
import com.a10miaomiao.bilimiao.comm.utils.MiaoLogger
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * 把 KMP 迁移前的屏蔽数据合并到 Room 新库。
 *
 * 背景：旧实现用 SQLiteOpenHelper 打开 "filter_db"，文件落在 databases/ 目录；
 * 新实现用 Room 打开 files/filter_db（见 FilterDatabaseAndroid）。库名相同但目录不同，
 * 所以旧数据不会自动跟过来，需要在这里显式合并一次。
 *
 * 合并本身交给 [mergeFilterBackup]，与备份导入共用同一套“按内容取并集”逻辑，
 * 保证不会覆盖用户在新库里后加的屏蔽项。
 *
 * 合并成功后把旧库改名为 filter_db.migrated：改名既是“已完成”标记，也避免用户
 * 迁移后手动删除的屏蔽项在下次启动时又被迁移回来。
 */
internal object LegacyFilterMigrator {

    private const val LEGACY_DB_NAME = "filter_db"
    private const val MIGRATED_FILE_NAME = "filter_db.migrated"
    private const val TAG = "LegacyFilterMigrator"

    fun migrate(app: Application, db: FilterDatabase) {
        val legacyFile = app.getDatabasePath(LEGACY_DB_NAME)
        if (!legacyFile.exists()) {
            return
        }
        try {
            val result = migrateFrom(legacyFile, db)
            MiaoLogger(TAG).i("旧版屏蔽数据已合并：${result.summary()}")
            val migratedFile = File(legacyFile.parentFile, MIGRATED_FILE_NAME)
            if (legacyFile.renameTo(migratedFile)) {
                MiaoLogger(TAG).i("旧版屏蔽库已重命名为 ${migratedFile.name}")
            } else {
                MiaoLogger(TAG).e("旧版屏蔽库重命名失败，下次启动会重新校验（按内容去重，不会重复插入）")
            }
        } catch (e: Exception) {
            // 迁移失败不能影响屏蔽功能本身，保留旧库留待下次启动重试
            MiaoLogger(TAG).e("旧版屏蔽数据合并失败：${e.message}")
        }
    }

    private fun migrateFrom(legacyFile: File, db: FilterDatabase): FilterMergeResult {
        SQLiteDatabase.openDatabase(
            legacyFile.path,
            null,
            SQLiteDatabase.OPEN_READONLY,
        ).use { legacy ->
            // 旧版三张表由各自独立的 SQLiteOpenHelper 创建，
            // 用户没进过对应设置页时表可能不存在，需要先判断
            val backup = FilterBackupInfo(
                filterWords = if (legacy.hasTable("filter_world")) {
                    legacy.readStrings("filter_world", "keyword")
                } else {
                    emptyList()
                },
                filterTags = if (legacy.hasTable("filter_tag")) {
                    legacy.readStrings("filter_tag", "name")
                } else {
                    emptyList()
                },
                filterUppers = if (legacy.hasTable("filter_upper")) {
                    legacy.readUppers()
                } else {
                    emptyList()
                },
            )
            return runBlocking { mergeFilterBackup(db, backup) }
        }
    }

    private fun SQLiteDatabase.hasTable(tableName: String): Boolean =
        rawQuery(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(tableName),
        ).use { it.moveToFirst() }

    private fun SQLiteDatabase.readStrings(tableName: String, column: String): List<String> =
        query(tableName, arrayOf(column), null, null, null, null, "id ASC").use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    if (!cursor.isNull(0)) {
                        add(cursor.getString(0))
                    }
                }
            }
        }

    private fun SQLiteDatabase.readUppers(): List<FilterBackupUpper> =
        rawQuery("SELECT mid, name FROM filter_upper", null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    val name = if (cursor.isNull(1)) "" else cursor.getString(1)
                    add(FilterBackupUpper(mid = cursor.getLong(0), name = name))
                }
            }
        }
}
