package cn.a10miaomiao.bilimiao.danmaku.platform

/**
 * XML 解析 expect 声明
 * 将字节数组形式的 XML 解析后，通过回调通知给 BiliDanmakuParseHelper
 */
expect fun parseBiliDanmakuXml(
    input: ByteArray,
    onElementStart: (uri: String?, localName: String, qName: String?, attributes: Map<String, String>) -> Unit,
    onElementEnd: (uri: String?, localName: String, qName: String?) -> Unit,
    onCharacters: (text: String) -> Unit
)
