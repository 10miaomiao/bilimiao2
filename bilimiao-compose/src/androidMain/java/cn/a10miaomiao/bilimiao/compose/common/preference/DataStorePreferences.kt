package cn.a10miaomiao.bilimiao.compose.common.preference

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import me.zhanghai.compose.preference.MutablePreferences
import me.zhanghai.compose.preference.Preferences

private typealias AndroidXPreferences = androidx.datastore.preferences.core.Preferences
private typealias AndroidXMutablePreferences = androidx.datastore.preferences.core.MutablePreferences

internal class DataStorePreferences(
    val preferences: AndroidXPreferences? = null,
) : Preferences {

    private val map = preferences?.run {
        asMap().mapKeys { it.key.name }
    } ?: mapOf()

    override fun <T> get(key: String): T? = map[key] as T

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences {
        return MutableDataStorePreferences(
            preferences?.toMutablePreferences(),
        )
    }
}

internal class MutableDataStorePreferences(
    val preferences: AndroidXMutablePreferences? = null,
) : MutablePreferences {

    private val map = preferences?.run {
        asMap().mapKeys { it.key.name }.toMutableMap()
    } ?: mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(key: String): T? = map[key] as T?

    override fun asMap(): Map<String, Any> = map

    override fun toMutablePreferences(): MutablePreferences =
        MutableDataStorePreferences(preferences?.toMutablePreferences())

    override fun <T> set(key: String, value: T?) {
        if (map[key] == value) {
            return
        }
        map[key] = value as Any
        preferences?.let {
            when (value) {
                is Boolean -> it[booleanPreferencesKey(key)] = value
                is Int -> it[intPreferencesKey(key)] = value
                is Long -> it[longPreferencesKey(key)] = value
                is Float -> it[floatPreferencesKey(key)] = value
                is String -> it[stringPreferencesKey(key)] = value
                is Set<*> -> @Suppress("UNCHECKED_CAST") {
                    it[stringSetPreferencesKey(key)] = value as Set<String>
                }
            }
        }
    }

    override fun clear() {
        preferences?.clear()
    }
}
