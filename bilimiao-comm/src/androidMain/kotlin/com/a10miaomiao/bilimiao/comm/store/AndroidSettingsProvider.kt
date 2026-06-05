package com.a10miaomiao.bilimiao.comm.store

import android.content.Context
import android.content.SharedPreferences
import com.a10miaomiao.bilimiao.comm.BilimiaoCommCore

class AndroidSettingsProvider(
    context: Context,
) : SettingsProvider {

    private val sp = context.getSharedPreferences(BilimiaoCommCore.APP_NAME, Context.MODE_PRIVATE)

    override fun getString(key: String, defaultValue: String): String {
        return sp.getString(key, defaultValue) ?: defaultValue
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return sp.getInt(key, defaultValue)
    }

    override fun edit(): SettingsEditor {
        return AndroidSettingsEditor(sp.edit())
    }
}

private class AndroidSettingsEditor(
    private val editor: SharedPreferences.Editor,
) : SettingsEditor {

    override fun putString(key: String, value: String): SettingsEditor {
        editor.putString(key, value)
        return this
    }

    override fun putInt(key: String, value: Int): SettingsEditor {
        editor.putInt(key, value)
        return this
    }

    override fun apply() {
        editor.apply()
    }
}
