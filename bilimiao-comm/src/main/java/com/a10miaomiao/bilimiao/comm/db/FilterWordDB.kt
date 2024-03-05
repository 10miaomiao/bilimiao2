package com.a10miaomiao.bilimiao.comm.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList

class FilterWordDB(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        private const val DB_NAME = "filter_db"
        private const val TABLE_NAME = "filter_world" //TODO fix typo
        private val CREATE_TABLE = """create table if not exists $TABLE_NAME
            |(id integer primary key autoincrement,
            |keyword text)""".trimMargin()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE)//创建表
    }

    override fun onOpen(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

    }

    /**
     * 查询全部搜索记录
     */
    fun queryAll(): ArrayList<String> {
        val historys = ArrayList<String>()
        //获取数据库对象
        val db = readableDatabase
        //查询表中的数据
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "id asc")
        //获取name列的索引
        cursor.moveToFirst()
        while (!cursor.isAfterLast) {
            val history = cursor.getString(1)
            historys.add(history)
            cursor.moveToNext()
        }
        cursor.close()//关闭结果集
        db.close()//关闭数据库对象
        return historys
    }

    /**
     * 插入数据到数据库
     */
    fun insert(keyword: String) {
        val db = writableDatabase
        //生成ContentValues对象
        val cv = ContentValues()
        //往ContentValues对象存放数据，键-值对模式
        cv.put("keyword", keyword)
        //调用insert方法，将数据插入数据库
        db.insert(TABLE_NAME, null, cv)
        //关闭数据库
        db.close()
    }

    fun updateKeyword(old: String, new: String) {
        val db = writableDatabase
        //生成ContentValues对象
        val cv = ContentValues()
        //往ContentValues对象存放数据，键-值对模式
        cv.put("keyword", new)
        //调用insert方法，将数据插入数据库
        db.update(TABLE_NAME, cv, "keyword=?", arrayOf(old))
        //关闭数据库
        db.close()
    }

    /**
     * 删除某条数据
     */
    fun deleteByKeyword(keyword: String) {
        val db = writableDatabase
        //生成ContentValues对象
        db.delete(TABLE_NAME, "keyword=?", arrayOf(keyword))
        //关闭数据库
        db.close()
    }

    /**
     * 按序号删除某条数据
     */
    fun deleteById(index: Int) {
        val db = writableDatabase
        //生成ContentValues对象
        db.delete(TABLE_NAME, "id=?", arrayOf(index.toString()))
        //关闭数据库
        db.close()
    }

    /**
     * 删除全部数据
     */
    fun deleteAll() {
        val db = writableDatabase
        //删除全部数据
        db.execSQL("delete from $TABLE_NAME")
        //关闭数据库
        db.close()
    }
}