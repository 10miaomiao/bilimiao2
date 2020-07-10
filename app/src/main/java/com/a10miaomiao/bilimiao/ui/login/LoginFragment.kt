package com.a10miaomiao.bilimiao.ui.login

import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.os.TokenWatcher
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.netword.LoginHelper
import com.a10miaomiao.bilimiao.store.Store
import com.a10miaomiao.bilimiao.ui.commponents.headerView
import com.a10miaomiao.bilimiao.ui.rank.RankCategoryDetailsViewModel
import com.a10miaomiao.bilimiao.utils.DebugMiao
import com.a10miaomiao.bilimiao.utils.getViewModel
import com.a10miaomiao.bilimiao.utils.newViewModelFactory
import com.a10miaomiao.bilimiao.utils.startFragment
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_video_info.view.*
import me.yokeyword.fragmentation_swipeback.SwipeBackFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.design.textInputEditText
import org.jetbrains.anko.design.textInputLayout
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.dip

class LoginFragment : SwipeBackFragment() {

    lateinit var viewModel: LoginViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = getViewModel { LoginViewModel(context!!) }
        return attachToSwipeBack(render().view)
    }

    override fun onSupportVisible() {
        super.onSupportVisible()
        val loginInfo = Bilimiao.app.loginInfo
        if (loginInfo != null){
            viewModel.authInfo(loginInfo)
        }
    }

    private fun EditText.onTextChanged(event: (text: String) -> Unit) {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                event(s!!.toString())
            }
        })
    }

    private fun ViewManager.header() {
        val observePasswordHasFocus = +viewModel.passwordHasFocus
        val headerHeight = dip(72)
        linearLayout {
            lparams {
                height = headerHeight
                width = matchParent
            }

            imageView {
                observePasswordHasFocus {
                    imageResource = if (it)
                        R.drawable.ic_22_hide
                    else
                        R.drawable.ic_22
                }
            }.lparams {
                width = headerHeight
                gravity = Gravity.START
            }
            textView("登录bilibili") {
                textSize = 24f
                gravity = Gravity.CENTER
            }.lparams(matchParent, matchParent) {
                weight = 1f
            }
            imageView {
                observePasswordHasFocus {
                    imageResource = if (it)
                        R.drawable.ic_33_hide
                    else
                        R.drawable.ic_33
                }
            }.lparams {
                width = headerHeight
                gravity = Gravity.END
            }
        }
    }

    private fun ViewManager.form() {
        val observeUsername = +viewModel.username
        val observePassword = +viewModel.password
        val observeCaptcha = +viewModel.captcha
        val observeCaptchaUrl = +viewModel.captchaUrl
        val observeIsCaptcha = +viewModel.isCaptcha

        verticalLayout {
            lparams {
                topMargin = -dip(5)
                width = matchParent
                height = matchParent
            }
            applyRecursively(ViewStyle.roundRect(dip(5)))
            backgroundColor = config.blockBackgroundColor
            verticalPadding = dip(15)
            horizontalPadding = dip(10)
            gravity = Gravity.CENTER_HORIZONTAL

            textInputLayout {
                editText {
                    hint = "请输入用户名/邮箱/手机号"
                    onTextChanged { viewModel.username set it }
                    observeUsername { if (it != text.toString()) setText(it) }
                }
            }

            textInputLayout {
                editText {
                    hint = "请输入密码"
                    inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD or InputType.TYPE_CLASS_TEXT
                    onTextChanged { viewModel.password set it }
                    observePassword { if (it != text.toString()) setText(it) }
                    setOnFocusChangeListener { v, hasFocus ->
                        viewModel.passwordHasFocus set hasFocus
                    }
                }
            }

            linearLayout {
                gravity = Gravity.CENTER
                observeIsCaptcha {
                    visibility = if (it) View.VISIBLE else View.GONE
                }

                textInputLayout {
                    editText {
                        hint = "请输入验证码"
                        onTextChanged { viewModel.captcha set it }
                        observeCaptcha { if (it != text.toString()) setText(it) }
                    }
                }.lparams(weight = 1f)

                imageView {
                    observeCaptchaUrl { url ->
                        LoginHelper.getCaptchaImage(url)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({
                                    setImageBitmap(it)
                                }, {
                                    //TODO: 我就是不处理，手动傲娇(￣_,￣ )
                                })
                    }
                    setOnClickListener {
                        viewModel.captchaUrl set viewModel.getCaptchaUrl()
                    }
                }.lparams{
                    leftMargin = dip(5)
                }
            }

            textView("注：你无论怎么输，验证码都是错的"){
                observeIsCaptcha {
                    visibility = if (it) View.VISIBLE else View.GONE
                }
            }


            button("登录") {
                setOnClickListener { viewModel.login() }
            }

            textView("网页登录"){
                textColor = config.themeColor
                setOnClickListener {
                    startFragment(H5LoginFragment.newInstance())
                }
            }.lparams {
                verticalMargin = dip(10)
            }

        }
    }

    private fun render() = UI {
        verticalLayout {
            headerView {
                title("登录")
                navigationIcon(R.drawable.ic_arrow_back_white_24dp)
                navigationOnClick {
                    pop()
                }
            }
            header()
            form()
        }
    }

}