package cn.a10miaomiao.bilimiao.cover

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Outline
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import net.mikaelzero.mojito.ext.mojito
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

        fun launch(context: Context, id: String) {
            if (id.startsWith("BV")) {
                launch(context, id, "BV")
            } else {
                launch(context, id, "AV")
            }
        }
    }

    private lateinit var viewModel: CoverViewModel

    private val mMotionLayout by lazy { findViewById<MotionLayout>(R.id.mMotionLayout) }
    private val mMainContainerLl by lazy { findViewById<LinearLayout>(R.id.mMainContainerLl) }
    private val mBackground by lazy { findViewById<View>(R.id.mBackground) }
    private val mSaveCoverLl by lazy { findViewById<LinearLayout>(R.id.mSaveCoverLl) }
    private val mTitleTv by lazy { findViewById<TextView>(R.id.mTitleTv) }
    private val mIDTv by lazy { findViewById<TextView>(R.id.mIDTv) }
    private val mPermissionTv by lazy { findViewById<TextView>(R.id.mPermissionTv) }
    private val mProgress by lazy { findViewById<ProgressBar>(R.id.mProgress) }
    private val mCoverIv by lazy { findViewById<ImageView>(R.id.mCoverIv) }
    private val mMoreIv by lazy { findViewById<ImageView>(R.id.mMoreIv) }
    private val mBtnBox1 by lazy { findViewById<FrameLayout>(R.id.mBtnBox1) }
    private val mBtnBox2 by lazy { findViewById<FrameLayout>(R.id.mBtnBox2) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cover)
        initViewModel()
        initArgument()
        initView()
        // 安卓Q以上版本保存至公共的媒体文件夹，不需要存储权限
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            requestPermissions()
        }
    }

    private fun initArgument() {
        intent.extras?.let { extras ->
            viewModel.setConfig("", "少女正在祈祷")
            if (extras.containsKey(Intent.EXTRA_TEXT)) {
                val text = extras.getString(Intent.EXTRA_TEXT)!!
                val urlInfo = BiliUrlMatcher.findIDByUrl(text)
                val type = urlInfo[0].toUpperCase()
                val id = urlInfo[1]
                if (type == "未知类型") {
                    val textList = text.split(" ")
                    if (textList.size > 1) {
                        val url = textList[textList.size - 1]
                        if (url.indexOf("https://") == 0
                            || url.indexOf("http://") == 0) {
                            viewModel.resolveUrl(url)
                        }
                    }
                } else {
                    viewModel.setConfig(type, id)
                }
                Unit
            } else if (extras.containsKey("id") && extras.containsKey("type")) {
                val type = extras.getString("type")!!.toUpperCase()
                val id = extras.getString("id")!!
                viewModel.setConfig(type, id)
            } else {
                viewModel.setConfig("", "未知")
            }
        }
    }

    private fun initView() {
        mMotionLayout.transitionToEnd()
        mMotionLayout.setTransitionListener(object : MotionLayout.TransitionListener {
            override fun onTransitionStarted(p0: MotionLayout?, p1: Int, p2: Int) {
            }

            override fun onTransitionChange(motionLayout: MotionLayout, i: Int, i1: Int, v: Float) {

            }

            override fun onTransitionCompleted(motionLayout: MotionLayout, p1: Int) {
                if (motionLayout.progress == 0f) {
                    finish()
                }
            }

            override fun onTransitionTrigger(motionLayout: MotionLayout, p1: Int, p2: Boolean, p3: Float) {
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
        mCoverIv.setOnClickListener {
            viewModel.coverUrl.value?.let { url ->
                mCoverIv.mojito(url)
            }
        }
        mMoreIv.setOnClickListener {
            val popupMenu = PopupMenu(this, it)
            popupMenu.inflate(R.menu.cover)
            popupMenu.setOnMenuItemClickListener(this::onMenuItemClick)
            popupMenu.show()
        }
        mSaveCoverLl.setOnClickListener {
            val bitmap = viewModel.coverBitmap.value
            if (bitmap == null) {
                toast("图片未加载")
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val fileName = "${viewModel.fileName.value ?: "未命名"}.jpg"
                saveSignImage(fileName, bitmap)
                toast("已保存至系统相册，文件名:${fileName}")
            } else if (requestPermissions()) { //判断有没有存储权限
                saveImage(bitmap)
            }
        }
    }

    private fun initViewModel() {
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return CoverViewModel(this@CoverActivity) as T
                }
            },
        )[CoverViewModel::class.java]
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
        viewModel.fileName.observe(this) {
            mIDTv.text = it
            mPermissionTv.text = "文件名：$it.jpg"
        }
    }

    fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cover_custom -> {
                toast("施工中")
            }
            R.id.cover_copy -> {
                viewModel.coverUrl.value?.let {
                    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clipData = ClipData.newPlainText("imageUrl", it)
                    clipboardManager.setPrimaryClip(clipData)
                    toast("图片链接已复制到剪切板")
                }
            }
            R.id.cover_more -> {
                viewModel.openMore()
            }
        }
        return true
    }


    /**
     * 保存图片
     */
    private fun saveImage(bitmap: Bitmap) {
        try {
            // 判断文件夹是否已经创建
            File(path).let {
                if (!it.exists()) {
                    val b = it.mkdirs()
                }
            }
            // 保存图片文件
            File("$path${viewModel.fileName.value ?: "未命名"}.jpg").let {
                it.writeBitmap(bitmap)
                toast("图片已保存至 ${it.path}")
                notifyPhoto(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
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

    /**
     * 将文件保存到公共的媒体文件夹
     */
    fun saveSignImage(fileName: String, bitmap: Bitmap) {
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)

            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
            //RELATIVE_PATH是相对路径不是绝对路径
            //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/BilimiaoCover")
            //contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Music/signImage");
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val uri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                //若生成了uri，则表示该文件添加成功
                //使用流将内容写入该uri中即可
                val outputStream = contentResolver.openOutputStream(uri)
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                    outputStream.flush()
                    outputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun dip(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
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
            mPermissionTv.text = "文件名：${viewModel.fileName.value ?: "未命名"}.jpg"
        }
    }

    override fun onBackPressed() {
        mMotionLayout.transitionToStart()
    }
}