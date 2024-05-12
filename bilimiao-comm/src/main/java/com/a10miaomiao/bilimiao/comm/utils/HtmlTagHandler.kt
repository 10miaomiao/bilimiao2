package com.a10miaomiao.bilimiao.comm.utils

import android.R
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.*
import android.text.style.*
import org.xml.sax.*
import java.io.StringReader
import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory


class HtmlTagHandler {

    companion object {
        fun fromHtml(
            source: String,
            getter: Html.ImageGetter? = null,
            tagHandler: TagHandler? = null,
        ): Spanned {
            val h = HtmlToSpannedConverter(source, getter, tagHandler)
            return h.convert()
        }

        private val testStyleMap = mapOf<String, TextStyle>(
            "keyword" to TextStyle(
                typeface = Typeface.BOLD
            )
        )
    }

    class HtmlToSpannedConverter(
        val mSource: String,
        val mImageGetter: Html.ImageGetter?,
        val mTagHandler: TagHandler?,
    ) : ContentHandler {

        private val HEADER_SIZES = floatArrayOf(
            1.5f, 1.4f, 1.3f, 1.2f, 1.1f, 1f
        )

        private val mReader: XMLReader? = null
        private var mSpannableStringBuilder = SpannableStringBuilder()


        fun convert(): Spanned {
            try {
                val saxFactory: SAXParserFactory = SAXParserFactory.newInstance()
                val saxParser: SAXParser = saxFactory.newSAXParser() //利用获取到的对象创建一个解析器
                val xmlReader: XMLReader = saxFactory.newSAXParser().xmlReader //获取一个XMLReader
                xmlReader.contentHandler = this
                xmlReader.parse(InputSource(StringReader(mSource)))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Fix flags and range for paragraph-type markup.
            val obj: Array<out ParagraphStyle> = mSpannableStringBuilder.getSpans(
                0, mSpannableStringBuilder.length,
                ParagraphStyle::class.java
            )
            for (i in obj.indices) {
                val start = mSpannableStringBuilder.getSpanStart(obj[i])
                var end = mSpannableStringBuilder.getSpanEnd(obj[i])

                // If the last line of the range is blank, back off by one.
                if (end - 2 >= 0) {
                    if (mSpannableStringBuilder[end - 1] == '\n' &&
                        mSpannableStringBuilder[end - 2] == '\n'
                    ) {
                        end--
                    }
                }
                if (end == start) {
                    mSpannableStringBuilder.removeSpan(obj[i])
                } else {
                    mSpannableStringBuilder.setSpan(obj[i], start, end, Spannable.SPAN_PARAGRAPH)
                }
            }
            return mSpannableStringBuilder
        }

        private fun handleStartTag(tag: String, attributes: Attributes) {
            val className: String? = attributes.getValue("", "class")
            val classNames = if (className?.isNotBlank() == true) {
                className.split(' ')
            } else { emptyList() }
            if (tag.equals("br", ignoreCase = true)) {
                // We don't need to handle this. TagSoup will ensure that there's a </br> for each <br>
                // so we can safely emite the linebreaks when we handle the close tag.
            } else if (tag.equals("p", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
            } else if (tag.equals("div", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
            } else if (tag.equals("strong", ignoreCase = true)) {
                start(mSpannableStringBuilder, Bold())
            } else if (tag.equals("b", ignoreCase = true)) {
                start(mSpannableStringBuilder, Bold())
            } else if (tag.equals("em", ignoreCase = true)) {
                start(mSpannableStringBuilder, TextTag(classNames, Italic()))
            } else if (tag.equals("cite", ignoreCase = true)) {
                start(mSpannableStringBuilder, Italic())
            } else if (tag.equals("dfn", ignoreCase = true)) {
                start(mSpannableStringBuilder, Italic())
            } else if (tag.equals("i", ignoreCase = true)) {
                start(mSpannableStringBuilder, Italic())
            } else if (tag.equals("big", ignoreCase = true)) {
                start(mSpannableStringBuilder, Big())
            } else if (tag.equals("small", ignoreCase = true)) {
                start(mSpannableStringBuilder, Small())
            } else if (tag.equals("font", ignoreCase = true)) {
                startFont(mSpannableStringBuilder, attributes)
            } else if (tag.equals("blockquote", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
                start(mSpannableStringBuilder, Blockquote())
            } else if (tag.equals("tt", ignoreCase = true)) {
                start(mSpannableStringBuilder, Monospace())
            } else if (tag.equals("a", ignoreCase = true)) {
                startA(mSpannableStringBuilder, attributes)
            } else if (tag.equals("u", ignoreCase = true)) {
                start(mSpannableStringBuilder, Underline())
            } else if (tag.equals("sup", ignoreCase = true)) {
                start(mSpannableStringBuilder, Super())
            } else if (tag.equals("sub", ignoreCase = true)) {
                start(mSpannableStringBuilder, Sub())
            } else if (tag.length == 2 && tag[0].lowercaseChar() == 'h' && tag[1] >= '1' && tag[1] <= '6') {
                handleP(mSpannableStringBuilder)
                start(mSpannableStringBuilder, Header(tag[1] - '1'))
            } else if (tag.equals("img", ignoreCase = true)) {
                startImg(mSpannableStringBuilder, attributes, mImageGetter)
            } else {
                mTagHandler?.handleTag(true, tag, mSpannableStringBuilder, attributes)
            }
        }

        private fun handleEndTag(tag: String) {
            if (tag.equals("br", ignoreCase = true)) {
                handleBr(mSpannableStringBuilder)
            } else if (tag.equals("p", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
            } else if (tag.equals("div", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
            } else if (tag.equals("strong", ignoreCase = true)) {
                end(mSpannableStringBuilder, Bold::class.java, StyleSpan(Typeface.BOLD))
            } else if (tag.equals("b", ignoreCase = true)) {
                end(mSpannableStringBuilder, Bold::class.java, StyleSpan(Typeface.BOLD))
            } else if (tag.equals("em", ignoreCase = true)) {
                end(mSpannableStringBuilder, TextTag::class.java, StyleSpan(Typeface.ITALIC))
            } else if (tag.equals("cite", ignoreCase = true)) {
                end(mSpannableStringBuilder, Italic::class.java, StyleSpan(Typeface.ITALIC))
            } else if (tag.equals("dfn", ignoreCase = true)) {
                end(mSpannableStringBuilder, Italic::class.java, StyleSpan(Typeface.ITALIC))
            } else if (tag.equals("i", ignoreCase = true)) {
                end(mSpannableStringBuilder, Italic::class.java, StyleSpan(Typeface.ITALIC))
            } else if (tag.equals("big", ignoreCase = true)) {
                end(mSpannableStringBuilder, Big::class.java, RelativeSizeSpan(1.25f))
            } else if (tag.equals("small", ignoreCase = true)) {
                end(mSpannableStringBuilder, Small::class.java, RelativeSizeSpan(0.8f))
            } else if (tag.equals("font", ignoreCase = true)) {
                endFont(mSpannableStringBuilder)
            } else if (tag.equals("blockquote", ignoreCase = true)) {
                handleP(mSpannableStringBuilder)
                end(mSpannableStringBuilder, Blockquote::class.java, QuoteSpan())
            } else if (tag.equals("tt", ignoreCase = true)) {
                end(
                    mSpannableStringBuilder, Monospace::class.java,
                    TypefaceSpan("monospace")
                )
            } else if (tag.equals("a", ignoreCase = true)) {
                endA(mSpannableStringBuilder)
            } else if (tag.equals("u", ignoreCase = true)) {
                end(mSpannableStringBuilder, Underline::class.java, UnderlineSpan())
            } else if (tag.equals("sup", ignoreCase = true)) {
                end(mSpannableStringBuilder, Super::class.java, SuperscriptSpan())
            } else if (tag.equals("sub", ignoreCase = true)) {
                end(mSpannableStringBuilder, Sub::class.java, SubscriptSpan())
            } else if (tag.length == 2 && tag[0].lowercaseChar() == 'h' && tag[1] >= '1' && tag[1] <= '6') {
                handleP(mSpannableStringBuilder)
                endHeader(mSpannableStringBuilder)
            } else {
                mTagHandler?.handleTag(false, tag, mSpannableStringBuilder, null)
            }
        }

        private fun handleP(text: SpannableStringBuilder) {
            val len: Int = text.length
            if (len >= 1 && text[len - 1] == '\n') {
                if (len >= 2 && text[len - 2] == '\n') {
                    return
                }
                text.append("\n")
                return
            }
            if (len != 0) {
                text.append("\n\n")
            }
        }

        private fun handleBr(text: SpannableStringBuilder) {
            text.append("\n")
        }

        private fun getLast(text: Spanned, kind: Class<*>): Any? {
            /*
         * This knows that the last returned object from getSpans()
         * will be the most recently added.
         */
            val objs = text.getSpans(0, text.length, kind)
            return if (objs.isEmpty()) {
                null
            } else {
                objs[objs.size - 1]
            }
        }

        private fun start(text: SpannableStringBuilder, mark: Any) {
            val len: Int = text.length
            text.setSpan(mark, len, len, Spannable.SPAN_MARK_MARK)
        }

        private fun end(
            text: SpannableStringBuilder,
            kind: Class<*>,
            repl: Any
        ) {
            var what = repl
            val len: Int = text.length
            val obj = getLast(text, kind)
            val where = text.getSpanStart(obj)
            if (obj is TextTag) {
                var textStyle = obj.textStyle
                obj.classNames.forEach { className ->
                    testStyleMap[className]?.let { textStyle += it }
                }
                what = StyleSpan(textStyle.typeface ?: Typeface.NORMAL)
            }
            text.removeSpan(obj)
            if (where != len) {
                text.setSpan(what, where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }

        private fun startImg(
            text: SpannableStringBuilder,
            attributes: Attributes, img: Html.ImageGetter?
        ) {
            val src = attributes.getValue("", "src")
            var d: Drawable
            if (img != null) {
                d = img.getDrawable(src)
            } else {
                d = Resources.getSystem().getDrawable(R.drawable.ic_delete)
                d.setBounds(0, 0, d.intrinsicWidth, d.intrinsicHeight)
            }
            val len: Int = text.length
            text.append("\uFFFC")
            text.setSpan(
                ImageSpan(d, src), len, text.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        private fun startFont(
            text: SpannableStringBuilder,
            attributes: Attributes
        ) {
            val color = attributes.getValue("", "color")
            val face = attributes.getValue("", "face")
            val len: Int = text.length
            text.setSpan(Font(color, face), len, len, Spannable.SPAN_MARK_MARK)
        }

        private fun endFont(text: SpannableStringBuilder) {
            val len: Int = text.length
            val obj = getLast(text, Font::class.java)
            val where = text.getSpanStart(obj)
            text.removeSpan(obj)
            if (where != len) {
                val f = obj as Font?
                if (!TextUtils.isEmpty(f!!.mColor)) {
                    if (f.mColor.startsWith("@")) {
                        val res: Resources = Resources.getSystem()
                        val name = f.mColor.substring(1)
                        val colorRes: Int = res.getIdentifier(name, "color", "android")
                        if (colorRes != 0) {
                            val colors: ColorStateList = res.getColorStateList(colorRes)
                            text.setSpan(
                                TextAppearanceSpan(null, 0, 0, colors, null),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    } else {
                        val c: Int = Color.parseColor(f.mColor) //Color.getHtmlColor(f.mColor);
                        if (c != -1) {
                            text.setSpan(
                                ForegroundColorSpan(c or -0x1000000),
                                where, len,
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                            )
                        }
                    }
                }
                if (f.mFace != null) {
                    text.setSpan(
                        TypefaceSpan(f.mFace), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        private fun startA(text: SpannableStringBuilder, attributes: Attributes) {
            val href = attributes.getValue("", "href")
            val len: Int = text.length
            text.setSpan(Href(href), len, len, Spannable.SPAN_MARK_MARK)
        }

        private fun endA(text: SpannableStringBuilder) {
            val len: Int = text.length
            val obj = getLast(text, Href::class.java)
            val where = text.getSpanStart(obj)
            text.removeSpan(obj)
            if (where != len) {
                val h = obj as Href?
                if (h!!.mHref != null) {
                    text.setSpan(
                        URLSpan(h.mHref), where, len,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
        }

        private fun endHeader(text: SpannableStringBuilder) {
            var len: Int = text.length
            val obj = getLast(text, Header::class.java)
            val where = text.getSpanStart(obj)
            text.removeSpan(obj)

            // Back off not to change only the text, not the blank line.
            while (len > where && text[len - 1] === '\n') {
                len--
            }
            if (where != len) {
                val h = obj as Header?
                text.setSpan(
                    RelativeSizeSpan(HEADER_SIZES[h!!.mLevel]),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                text.setSpan(
                    StyleSpan(Typeface.BOLD),
                    where, len, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
        }

        override fun setDocumentLocator(locator: Locator?) {
        }

        override fun startDocument() {
        }

        override fun endDocument() {
        }

        override fun startPrefixMapping(prefix: String?, uri: String?) {
        }

        override fun endPrefixMapping(prefix: String?) {
        }


        override fun startElement(
            uri: String,
            localName: String,
            qName: String,
            attributes: Attributes
        ) {
            handleStartTag(localName, attributes);
        }

        override fun endElement(uri: String, localName: String, qName: String) {
            handleEndTag(localName)
        }

        override fun characters(ch: CharArray?, start: Int, length: Int) {
            val sb = StringBuilder()
            /*
             * Ignore whitespace that immediately follows other whitespace;
             * newlines count as spaces.
             */
            for (i in 0 until length) {
                val c = ch!![i + start]
                if (c == ' ' || c == '\n') {
                    var pred: Char
                    var len: Int = sb.length
                    if (len == 0) {
                        len = mSpannableStringBuilder.length
                        pred = if (len == 0) {
                            '\n'
                        } else {
                            mSpannableStringBuilder[len - 1]
                        }
                    } else {
                        pred = sb[len - 1]
                    }
                    if (pred != ' ' && pred != '\n') {
                        sb.append(' ')
                    }
                } else {
                    sb.append(c)
                }
            }

            mSpannableStringBuilder.append(sb)
        }


        override fun ignorableWhitespace(ch: CharArray?, start: Int, length: Int) {
        }

        override fun processingInstruction(target: String?, data: String?) {
        }

        override fun skippedEntity(name: String?) {
        }

    }

    private open class TextStyle(
        val typeface: Int? = null,
        val fontSize: Float? = null,
    ) {
        // TODO: 未完成，未适配更多样式
        open operator fun plus(textStyle: TextStyle): TextStyle {
            return TextStyle(
                typeface = textStyle.typeface ?: typeface,
                fontSize = textStyle.fontSize ?: fontSize
            )
        }
    }
    private open class TextTag(
        val classNames: List<String> = emptyList(),
        val textStyle: TextStyle,
    )
    private class Bold: TextStyle(typeface = Typeface.BOLD)
    private class Italic: TextStyle(typeface = Typeface.ITALIC)
    private class Underline
    private class Big: TextStyle(fontSize = 1.25f)
    private class Small: TextStyle(fontSize = 0.8f)
    private class Monospace
    private class Blockquote
    private class Super
    private class Sub
    private class Font(var mColor: String, var mFace: String)

    private class Href(var mHref: String)

    private class Header(val mLevel: Int)

    interface TagHandler {
        fun handleTag(open: Boolean, tag: String, output: Editable, attrs: Attributes?)
    }

}