package cn.a10miaomiao.bilimiao.compose.pages.user.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFavouriteViewModel
import org.kodein.di.compose.rememberInstance

internal sealed class FavouriteEditDialogState {
    data object Add : FavouriteEditDialogState()

    data class Update(
        val id: String,
        val cover: String,
        val title: String,
        val intro: String,
        val privacy: Int,
    ) : FavouriteEditDialogState()

    data class Delete(
        val id: String,
        val title: String,
    ) : FavouriteEditDialogState()
}

internal class FavouriteEditFormState(
    initialTitle: String,
    initialIntro: String,
    initialPrivacy: Int,
) {
    var title by mutableStateOf(initialTitle)
        private set
    var intro by mutableStateOf(initialIntro)
        private set

    var privacy by mutableIntStateOf(initialPrivacy)
        private set

    fun changeTitle(str: String) {
        title = str
    }

    fun changeIntro(str: String) {
        intro = str
    }

    fun changePrivacy(i: Int) {
        privacy = i
    }
}

@Composable
internal fun FavouriteEditForm(
    state: FavouriteEditFormState,
) {
    Column(
        modifier = Modifier.widthIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
    ) {
        TextField(
            value = state.title,
            onValueChange = state::changeTitle,
            label = {
                Text(text = "*名称")
            },
            placeholder = {
                Text(text = "收藏夹名称")
            }
        )
        TextField(
            value = state.intro,
            onValueChange = state::changeIntro,
            label = {
                Text(text = "简介")
            },
            placeholder = {
                Text(text = "收藏夹简介")
            }
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = state.privacy == 0,
                onCheckedChange = fun (checked) {
                    state.changePrivacy(if (checked) 0 else 1)
                }
            )
            Text(text = "公开")
        }
    }
}

@Composable
internal fun FavouriteEditDialog() {
    val viewModel: UserFavouriteViewModel by rememberInstance()

    val loading by remember {
        mutableStateOf(false)
    }

    val dialogState by viewModel.editDialogState.collectAsState()

    when (val state = dialogState) {
        is FavouriteEditDialogState.Add -> {

        }

        is FavouriteEditDialogState.Update -> {

        }

        is FavouriteEditDialogState.Delete -> {

        }

        null -> Unit
    }

}
