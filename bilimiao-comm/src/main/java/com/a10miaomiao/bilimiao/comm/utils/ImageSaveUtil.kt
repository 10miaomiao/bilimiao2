package com.a10miaomiao.bilimiao.comm.utils

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kongzue.dialogx.dialogs.PopTip
import java.io.File
import java.io.FileOutputStream

class ImageSaveUtil(
    val activity: Activity,
    val imageUrl: String,
) {

    private val path = Environment.getExternalStorageDirectory().path + "/BiliMiao/bili图片/"

    private val menuItems = arrayOf<String>(
        "保存图片",
        "复制图片链接",
    )

    fun showMemu(context: Context = activity) {
        MaterialAlertDialogBuilder(context)
            .setItems(menuItems) { _, i ->
                when(i) {
                    0 -> downloadAndSaveImage()
                    1 -> copyImageUrl()
                }
            }
            .show()
    }

    /**
     * 复制图片链接到剪切板
     */
    private fun copyImageUrl() {
        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("imageUrl", imageUrl)
        clipboardManager.setPrimaryClip(clipData)
        PopTip.show("图片链接已复制到剪切板")
    }

    /**
     * 下载并保存图片
     */
    private fun downloadAndSaveImage() {
        Glide.with(activity)
            .asBitmap()
            .load(imageUrl)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    isFirstResource: Boolean
                ): Boolean {
                    activity.runOnUiThread {
//                        loading.value = false
                    }
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap?,
                    model: Any?,
                    target: Target<Bitmap>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    activity.runOnUiThread {
                        resource?.let { saveImage(it) }
//                        loading.value = false
//                        coverBitmap.value = resource
                    }
                    return false
                }
            })
            .submit();
    }


    /**
     * 保存图片
     */
    private fun saveImage(bitmap: Bitmap) {
        try {
            val fileName = getFileName()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveImageToAlbum(fileName, bitmap)
                PopTip.show("已保存至系统相册，文件名:${fileName}")
                return
            } else if (
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
            ) {
                //判断是否有这个权限
                // 申请权限: 参数二：权限的数组；参数三：请求码
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return
            }
            // 判断文件夹是否已经创建
            File(path).let {
                if (!it.exists()) {
                    it.mkdirs()
                }
            }
            // 保存图片文件
            File(path + fileName).let {
                if (it.exists()) {
                    PopTip.show("图片已存在")
                    return
                }
                it.writeBitmap(bitmap)
                PopTip.show("图片已保存至 ${it.path}")
                notifyPhoto(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            PopTip.show("保存失败：" + e.message ?: e.toString())
        }
    }

    /**
     * 通知相册
     */
    private fun notifyPhoto(file: File) {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val uri = Uri.fromFile(file)
        intent.data = uri
        activity.sendBroadcast(intent)
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
    private fun saveImageToAlbum(fileName: String, bitmap: Bitmap) {
        try {
            //设置保存参数到ContentValues中
            val contentValues = ContentValues()
            //设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)

            //android Q中不再使用DATA字段，而用RELATIVE_PATH代替
            //RELATIVE_PATH是相对路径不是绝对路径
            //DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Bilimiao")
            //设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/JPEG")
            //执行insert操作，向系统文件夹中添加文件
            //EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            val contentResolver = activity.contentResolver

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
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

    private fun getFileName(): String {
        val urlArr = imageUrl.split("/")
        return urlArr.last()
    }

}