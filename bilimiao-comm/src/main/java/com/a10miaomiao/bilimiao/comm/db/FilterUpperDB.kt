package com.a10miaomiao.bilimiao.comm.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.ArrayList

class FilterUpperDB(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object {
        private const val DB_NAME = "filter_db"
        private const val TABLE_NAME = "filter_upper"
        private val CREATE_TABLE = """create table if not exists $TABLE_NAME
        |(mid integer primary key,
        |name text)""".trimMargin()
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
     * 查询全部记录
     */
    fun queryAll(): ArrayList<Upper> {
        val result = ArrayList<Upper>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        cursor.moveToFirst()
        val midIndex = cursor.getColumnIndex("mid")
        val nameIndex = cursor.getColumnIndex("name")
        while (!cursor.isAfterLast) {
            result += Upper(
                cursor.getLong(midIndex),
                cursor.getString(nameIndex)
            )
            cursor.moveToNext()
        }
        cursor.close()//关闭结果集
        db.close()//关闭数据库对象
        return result
    }

    /**
     * 插入数据到数据库
     */
    fun insert(mid: Long,name: String) {
        val db = writableDatabase
        //生成ContentValues对象
        val cv = ContentValues()
        //往ContentValues对象存放数据，键-值对模式
        cv.put("mid", mid)
        cv.put("name", name)
        //调用insert方法，将数据插入数据库
        db.insert(TABLE_NAME, null, cv)
        //关闭数据库
        db.close()
    }

    /**
     * 删除某条数据
     */
    fun deleteByMid(mid: Long) {
        val db = writableDatabase
        //生成ContentValues对象
        db.delete(TABLE_NAME, "mid=?", arrayOf(mid.toString()))
        //关闭数据库
        db.close()
    }


    data class Upper(
        val mid: Long,
        val name: String
    )
}