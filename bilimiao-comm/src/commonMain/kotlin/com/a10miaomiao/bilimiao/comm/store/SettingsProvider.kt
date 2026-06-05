package com.a10miaomiao.bilimiao.comm.store

interface SettingsProvider {
    fun getString(key: String, defaultValue: String): String
    fun getInt(key: String, defaultValue: Int): Int
    fun edit(): SettingsEditor
}

interface SettingsEditor {
    fun putString(key: String, value: String): SettingsEditor
    fun putInt(key: String, value: Int): SettingsEditor
    fun apply()
}
