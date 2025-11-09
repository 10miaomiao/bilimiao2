package cn.a10miaomiao.bilimiao.compose.base

interface PageSearchMethod {
    val name: String
    fun onSearch(keyword: String)
}