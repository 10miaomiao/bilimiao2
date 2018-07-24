package com.a10miaomiao.bilimiao.utils;

import android.content.Context

object SettingUtil{
    fun putString(context: Context, key: String, value: String) {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun putInt(context: Context, key: String, value: Int) {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putInt(key, value)
        editor.apply()
        //Log.d("value", value.toString())
    }
    fun putBoolean(context: Context, key: String, value: Boolean) {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(key, value)
        editor.apply()
        //Log.d("value", value.toString())
    }
    fun getString(context: Context, key: String, defValue: String): String {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        return sp.getString(key, defValue)
    }

    fun getInt(context: Context, key: String, defValue: Int): Int {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        return sp.getInt(key, defValue)
    }
    fun getBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        val sp = context.getSharedPreferences(ConstantUtil.APP_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(key, defValue)
    }
}