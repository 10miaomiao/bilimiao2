package com.a10miaomiao.bilimiao.comm.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList

class FilterTagDB(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        private const val DB_NAME = "filter_db"
        private const val TABLE_NAME = "filter_tag"
        private val CREATE_TABLE = """create table if not exists $TABLE_NAME
        |(id integer primary key autoincrement,
        |name text)""".trimMargin()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

    }

    fun queryAll(): ArrayList<String> {
        val tags = ArrayList<String>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "id asc")
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val tag = cursor.getString(1)
            tags.add(tag)
            cursor.moveToNext()
        }
        cursor.close()
        db.close()
        return tags
    }

    fun insert(tagName: String) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("name", tagName)
        db.insert(TABLE_NAME, null, cv)
        db.close()
    }

    fun updateTagName(old: String, new: String) {
        val db = writableDatabase
        val cv = ContentValues()
        cv.put("name", new)
        db.update(TABLE_NAME, cv, "name=?", arrayOf(old))
        db.close()
    }

    fun deleteByTagName(tagName: String) = writableDatabase.use {
        it.delete(TABLE_NAME, "name=?", arrayOf(tagName))
    }


    fun deleteById(index: Int)= writableDatabase.use {
        it.delete(TABLE_NAME, "id=?", arrayOf(index.toString()))
    }

    fun deleteAll() = writableDatabase.use {
        it.execSQL("delete from $TABLE_NAME")
    }

}