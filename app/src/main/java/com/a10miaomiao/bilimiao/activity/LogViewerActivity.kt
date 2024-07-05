package com.a10miaomiao.bilimiao.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.delegate.theme.ThemeDelegate
import com.a10miaomiao.bilimiao.config.config
import com.google.android.material.appbar.MaterialToolbar
import org.kodein.di.DI
import splitties.dimensions.dip
import splitties.experimental.InternalSplittiesApi
import splitties.views.backgroundColor
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.setContentView
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.view
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.core.wrapInScrollView

class LogViewerActivity : AppCompatActivity() {

    private val di: DI = DI.lazy {}

    private val themeDelegate by lazy {
        ThemeDelegate(this@LogViewerActivity, di)
    }

    private var mLogSummary = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeDelegate.onCreate(savedInstanceState)
        val ui = LogViewerUi(this)
        setContentView(ui)

        val logSummary = intent.getStringExtra("log_summary")
        if (!logSummary.isNullOrEmpty()) {
            mLogSummary = logSummary
            ui.logText.text = logSummary
        }
    }

    private fun copyLogText() {
        val logSummary = mLogSummary
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("log", logSummary)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "复制成功(●'◡'●)", Toast.LENGTH_SHORT).show()
    }

    @OptIn(InternalSplittiesApi::class)
    class LogViewerUi(
        val activity: LogViewerActivity
    ) : Ui {
        override val ctx = activity

        val toolBar = view<MaterialToolbar>(View.generateViewId()) {
            clipToPadding = true
            fitsSystemWindows = true
            setTitle(R.string.log_viewer)
            setNavigationOnClickListener {
                activity.onBackPressed()
            }
            menu.add(Menu.FIRST, 1, 0, "复制日志").also {
                it.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
            }
            setOnMenuItemClickListener {
                when(it.itemId) {
                    1 -> {
                        activity.copyLogText()
                    }
                }
                true
            }
        }

        val logText = textView {
            setPadding(dip(10))
            setTextIsSelectable(true)
        }

        override val root: View = verticalLayout {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            fitsSystemWindows = true

            addView(toolBar, lParams(matchParent, wrapContent))

            addView(verticalLayout {
                addView(logText, lParams(matchParent, wrapContent))
            }.wrapInScrollView {
                backgroundColor = config.windowBackgroundColor
            }, lParams(matchParent, matchParent))

        }
    }
}