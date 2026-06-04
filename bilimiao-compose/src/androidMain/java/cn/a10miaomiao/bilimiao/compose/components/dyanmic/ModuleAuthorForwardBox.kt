package cn.a10miaomiao.bilimiao.compose.components.dyanmic

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp

@Composable
fun ModuleAuthorForwardBox(
    authorForward: bilibili.app.dynamic.v2.ModuleAuthorForward
) {
    Text(
        buildAnnotatedString {
            authorForward.title.forEach {
                withLink(
                    LinkAnnotation.Url(
                        it.url,
                        TextLinkStyles(
                            style = SpanStyle(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    )
                ) {
                    append(it.text)
                }
            }
        },
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .padding(top = 5.dp),
    )
}