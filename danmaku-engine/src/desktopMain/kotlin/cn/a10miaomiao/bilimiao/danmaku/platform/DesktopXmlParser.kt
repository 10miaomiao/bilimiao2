package cn.a10miaomiao.bilimiao.danmaku.platform

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.ByteArrayInputStream
import javax.xml.parsers.SAXParserFactory

actual fun parseBiliDanmakuXml(
    input: ByteArray,
    onElementStart: (uri: String?, localName: String, qName: String?, attributes: Map<String, String>) -> Unit,
    onElementEnd: (uri: String?, localName: String, qName: String?) -> Unit,
    onCharacters: (text: String) -> Unit
) {
    val factory = SAXParserFactory.newInstance()
    factory.isNamespaceAware = true
    val parser = factory.newSAXParser()
    val handler = object : DefaultHandler() {
        override fun startElement(uri: String?, localName: String, qName: String?, attributes: Attributes) {
            val attrMap = mutableMapOf<String, String>()
            for (i in 0 until attributes.length) {
                val key = attributes.getLocalName(i).ifEmpty { attributes.getQName(i) }
                attrMap[key] = attributes.getValue(i)
            }
            onElementStart(uri, localName, qName, attrMap)
        }

        override fun endElement(uri: String?, localName: String, qName: String?) {
            onElementEnd(uri, localName, qName)
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            onCharacters(String(ch, start, length))
        }
    }
    parser.parse(ByteArrayInputStream(input), handler)
}
