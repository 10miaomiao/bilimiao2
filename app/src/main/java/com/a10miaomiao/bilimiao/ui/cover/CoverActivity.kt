package com.a10miaomiao.bilimiao.ui.cover

import android.Manifest
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.constraint.motion.MotionLayout
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.PopupMenu
import android.view.View
import android.view.ViewOutlineProvider
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.utils.ThemeUtil
import com.a10miaomiao.bilimiao.utils.newViewModelFactory
import kotlinx.android.synthetic.main.activity_cover.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.toast
import java.io.File
import java.io.FileOutputStream


class CoverActivity : AppCompatActivity() {

    private val path = Environment.getExternalStorageDirectory().path + "/BiliMiao/b站封面/"

    companion object {
        fun launch(context: Context, id: String, type: String) {
            val mIntent = Intent(context, CoverActivity::class.java)
            mIntent.putExtra("id", id)
            mIntent.putExtra("type", type)
            context.startActivity(mIntent)
        }
    }

    private lateinit var viewModel: CoverViewModel
    private var id = ""
    private var type = ""
    private var fileName = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)
        initArgument()
        fileName = type + id
        mIDTv.text = type + id
        mPermissionTv.text = "文件名：$fileName.jpg"
        initView()
        initViewModel()
        requestPermissions()
    }

    private fun initArgument() {
        val extras = intent.extras
        if (extras.containsKey(Intent.EXTRA_TEXT)) {
            val text = intent.extras.getString(Intent.EXTRA_TEXT)
            val urlInfo = BiliUrlMatcher.findIDByUrl(text)
            type = urlInfo[0].toUpperCase()
            id = urlInfo[1]
        } else if (extras.containsKey("id") && extras.containsKey("type")) {
            type = extras.getString("type").toUpperCase()
            id = extras.getString("id")
        } else {
            id = "未知"
        }
    }

    private fun initView() {
        mMotionLayout.transitionToEnd()
        mMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionChange(motionLayout: MotionLayout, i: Int, i1: Int, v: Float) {
                mBackground.alpha = v * 0.6f
            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, i: Int) {
                mBackground.alpha = motionLayout.progress * 0.6f
                if (motionLayout.progress == 0f) {
                    finish()
                }
            }
        })
        // 设置圆角
        val roundCorner = dip(36)
        mMainContainerLl.clipToOutline = true // 开启裁剪
        mMainContainerLl.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height + roundCorner,
                        roundCorner.toFloat())
            }
        }
        ViewStyle.roundRect(dip(24))(mBtnBox1)
        ViewStyle.roundRect(dip(24))(mBtnBox2)
        ViewStyle.roundRect(dip(10))(mCoverIv)
//        mColseIv.setOnClickListener {
//            mMotionLayout.transitionToStart()
//        }
        mMoreIv.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.cover)
//            popupMenu.setOnMenuItemClickListener(this::onMenuItemClick)
            popupMenu.show()
        }
        mSaveCoverLl.setOnClickListener {
            if (requestPermissions()) { //判断有没有存储权限
                saveImage()
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProviders.of(this, newViewModelFactory { CoverViewModel(this@CoverActivity, type, id) })
                .get(CoverViewModel::class.java)
        viewModel.coverBitmap.observe(this, Observer {
            if (it == null) {
                mProgress.visibility = View.VISIBLE
            } else {
                mProgress.visibility = View.GONE
                mCoverIv.setImageBitmap(it)
            }
        })
        viewModel.title.observe(this, Observer {
            it?.let { text -> mTitleTv.text = text }
        })
    }

    /**
     * 保存图片
     */
    private fun saveImage() {
        try {
            val bitmap = viewModel.coverBitmap.value
            if (bitmap == null) {
                toast("图片未加载")
                return
            }
            // 判断文件夹是否已经创建
            File(path).let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            // 保存图片文件
            File("$path$fileName.jpg").let {
                it.writeBitmap(bitmap)
                toast("图片已保存至 ${it.path}")
                notifyPhoto(it)
            }
        } catch (e: Exception) {
            toast("保存失败")
        }
    }

    /**
     * 通知相册
     */
    private fun notifyPhoto(file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val uri = Uri.fromFile(file)
        intent.data = uri
        sendBroadcast(intent)
    }

    /**
     * 保存图片
     */
    private fun File.writeBitmap(data: Bitmap) {
        val fOut = FileOutputStream(this)
        data.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
        fOut.flush()
        fOut.close()
    }


    //动态获取sd卡权限
    private fun requestPermissions(): Boolean {
        //判断是否6.0以上的手机 不是就不用
        if (Build.VERSION.SDK_INT >= 23) {
            //判断是否有这个权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                //2、申请权限: 参数二：权限的数组；参数三：请求码
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false //没有权限
            }
        }
        return true
    }

    /**
     * 判断授权的方法  授权成功直接调用写入方法  这是监听的回调
     * 参数  上下文   授权结果的数组   申请授权的数组
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            mPermissionTv.text = "请给予存储权限( •̀ ω •́ )✧"
        } else {
            mPermissionTv.text = "文件名：$fileName.jpg"
        }
    }

    override fun onBackPressed() {
        mMotionLayout.transitionToStart()
    }
}