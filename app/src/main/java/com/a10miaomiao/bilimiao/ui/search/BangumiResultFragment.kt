package com.a10miaomiao.bilimiao.ui.search

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.textView

class BangumiResultFragment : Fragment() {

    lateinit var _text: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return render().view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        SearchFragment.keyword.observe(this, Observer {
            _text.text = it
        })
    }

    private fun render() = UI {
        textView("番剧").let { _text = it }
    }
}