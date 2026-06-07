package cn.a10miaomiao.bilimiao.compose.common.preference

interface Preferences {
    fun <T> get(key: String): T?
    fun asMap(): Map<String, Any>
    fun toMutablePreferences(): MutablePreferences
}

interface MutablePreferences : Preferences {
    override fun toMutablePreferences(): MutablePreferences
    fun <T> set(key: String, value: T?)
    fun clear()
}
