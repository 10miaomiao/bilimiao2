package com.a10miaomiao.bilimiao.comm.store

import java.io.File
import java.util.Properties

class DesktopSettingsProvider(
    private val file: File,
) : SettingsProvider {

    private val properties = Properties().apply {
        if (file.exists()) {
            file.inputStream().use { load(it) }
        }
    }

    override fun getString(key: String, defaultValue: String): String {
        return properties.getProperty(key, defaultValue)
    }

    override fun getInt(key: String, defaultValue: Int): Int {
        return properties.getProperty(key, defaultValue.toString()).toIntOrNull() ?: defaultValue
    }

    override fun edit(): SettingsEditor {
        return DesktopSettingsEditor(properties, file)
    }
}

private class DesktopSettingsEditor(
    private val properties: Properties,
    private val file: File,
) : SettingsEditor {

    override fun putString(key: String, value: String): SettingsEditor {
        properties[key] = value
        return this
    }

    override fun putInt(key: String, value: Int): SettingsEditor {
        properties[key] = value.toString()
        return this
    }

    override fun apply() {
        file.parentFile?.mkdirs()
        file.outputStream().use { properties.store(it, null) }
    }
}
