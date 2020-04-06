package com.a10miaomiao.bilimiao.ui.home

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.widget.DrawerLayout
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.UserStore
import com.a10miaomiao.bilimiao.ui.MainActivity
import com.a10miaomiao.bilimiao.ui.login.LoginFragment
import com.a10miaomiao.bilimiao.ui.search.SearchFragment
import com.a10miaomiao.bilimiao.ui.setting.AboutFragment
import com.a10miaomiao.bilimiao.ui.setting.SettingFragment
import com.a10miaomiao.bilimiao.ui.theme.ThemeFragment
import com.a10miaomiao.bilimiao.ui.user.UserFragment
import com.a10miaomiao.bilimiao.utils.ConstantUtil
import com.a10miaomiao.bilimiao.utils.network
import com.a10miaomiao.bilimiao.utils.startFragment
import com.bumptech.glide.Glide
import me.yokeyword.fragmentation.SupportFragment
import org.jetbrains.anko.*
import org.jetbrains.anko.design.navigationView
import org.jetbrains.anko.support.v4.UI
import org.jetbrains.anko.support.v4.drawerLayout
import org.jetbrains.anko.support.v4.toast


class MainFragment : SupportFragment() {
    private val CONTAINER_ID = 233333

    private val homeFragment = HomeFragment()
    private val rankFragment = RankFragment()
    private val dowmloadFragment = DowmloadFragment()
    private val filterFragment = FilterFragment()
    private var mDrawerLayout: DrawerLayout? = null
    private lateinit var viewModel: MainViewModel
    private lateinit var userStore: UserStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        userStore = MainActivity.of(context!!).userStore
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        return render().view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initArgument()
        initView()
    }

    private fun initArgument() {
        val extras = arguments ?: Bundle()
        if (extras.containsKey(ConstantUtil.FROM_SHORTCUT)
                && extras.getBoolean(ConstantUtil.FROM_SHORTCUT)
                && extras.containsKey(ConstantUtil.SHORTCUT_NAME)) {
            when (extras.getString(ConstantUtil.SHORTCUT_NAME)) {
                ConstantUtil.SHORTCUT_SEARCH -> {
                    startFragment(SearchFragment.newInstance())
                }
                ConstantUtil.SHORTCUT_RANK -> {
                    viewModel.checkedMenuItemId set R.id.nav_rank
                }
            }
        }
    }

    fun openDrawer() {
        mDrawerLayout?.openDrawer(Gravity.LEFT)
    }

    private fun initView() {
        if (viewModel.currentFragment == null) {
            switchFragment(getFragment(-viewModel.checkedMenuItemId))
        } else {
            switchFragment(viewModel.currentFragment!!)
        }
        val observerMemu = +viewModel.checkedMenuItemId
        observerMemu { switchFragment(getFragment(it)) }
    }

    private fun getFragment(@IdRes itemId: Int): Fragment {
        return when (itemId) {
            R.id.nav_home -> homeFragment
            R.id.nav_rank -> rankFragment
            R.id.nav_dowmload -> dowmloadFragment
            R.id.nav_filter -> filterFragment
            else -> homeFragment
        }
    }

    private fun switchFragment(targetFragment: Fragment) {
        val trx = childFragmentManager.beginTransaction()
        viewModel.currentFragment?.let {
            if (it == targetFragment)
                return@switchFragment
            trx.hide(it)
        }
        if (!targetFragment.isAdded) {
            trx.add(CONTAINER_ID, targetFragment)
        }
        trx.show(targetFragment).commit()
        viewModel.currentFragment = targetFragment
    }

    private fun removeaAllFragment() {
        childFragmentManager
                .beginTransaction()
                .remove(homeFragment)
                .remove(rankFragment)
                .remove(filterFragment)
                .remove(dowmloadFragment)
                .commit()
    }

    private var lastBackTime = 0L

    override fun onBackPressedSupport(): Boolean {
        if (homeFragment.isHidden) {
            switchFragment(homeFragment)
            return true
        }
        val time = System.currentTimeMillis()
        if (time - lastBackTime < 2000) {
            return super.onBackPressedSupport()
        } else {
            lastBackTime = time
            toast("再按一次退出")
            return true
        }
    }

    private val navigationItemSelectedListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home,
            R.id.nav_rank,
            R.id.nav_dowmload,
            R.id.nav_filter -> {
                viewModel.checkedMenuItemId set item.itemId
            }
            //------------------------
            R.id.nav_theme -> {
                startFragment(ThemeFragment())
            }
            R.id.nav_about -> {
                startFragment(AboutFragment())
            }
            R.id.nav_setting -> {
                startFragment(SettingFragment())
            }
        }
        mDrawerLayout?.closeDrawers()
        true
    }

    private fun renderHeader() = UI {
        val observerUser = userStore.observer
        val observerNotNullUser = userStore.observeNotNull

        frameLayout {
            lparams(matchParent, dip(150))
            imageView {
                Glide.with(context)
                        .load(R.drawable.top_bg1)
                        .centerCrop()
                        .dontAnimate()
                        .into(this)
            }
            verticalLayout {
                padding = dip(10)

                imageView {
                    applyRecursively(ViewStyle.circle)
                    observerNotNullUser {
                        network(it.face)
                    }
                }.lparams {
                    width = dip(60)
                    height = dip(60)
                }

                textView {
                    textColor = Color.WHITE
                    textSize = 16f
                    observerNotNullUser {
                        text = it.name
                    }
                }.lparams {
                    topMargin = dip(5)
                }

                observerUser {
                    visibility = if (it == null) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
                }
                setOnClickListener {
                    startFragment(UserFragment.newInstance(userStore.user!!.mid))
                }
            }.lparams {
                width = matchParent
                height = wrapContent
                gravity = Gravity.BOTTOM
            }

            setOnLongClickListener {
                if (userStore.user != null)
                    return@setOnLongClickListener false
                startFragment(LoginFragment())
                true
            }
        }
    }

    private fun render() = UI {
        drawerLayout {
            let { mDrawerLayout = it }
            frameLayout {
                id = CONTAINER_ID
                backgroundColor = config.windowBackgroundColor
            }.lparams(matchParent, matchParent)

            val navigation = {
                navigationView {
                    inflateMenu(R.menu.activity_main_drawer)
                    setCheckedItem(-viewModel.checkedMenuItemId)
                    setNavigationItemSelectedListener(navigationItemSelectedListener)
                    addHeaderView(renderHeader().view)
                }.lparams {
                    width = wrapContent
                    height = matchParent
                    gravity = Gravity.START
                    backgroundColor = config.blockBackgroundColor
                }
            }

            MainActivity.of(context!!)
                    .themeUtil
                    .observeTheme(owner, Observer {
                        if (childCount >= 2)
                            removeViewAt(1)
                        navigation()
                    })
            navigation()
        }
    }


}