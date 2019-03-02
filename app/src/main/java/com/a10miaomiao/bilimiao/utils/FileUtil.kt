package com.a10miaomiao.bilimiao.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.google.gson.Gson
import java.io.*


/**
 * Created by 10喵喵 on 2017/8/28.
 */

class FileUtil(pathName: String,var context: Context? = null,var miao: Boolean = true){
    var path = Environment.getExternalStorageDirectory().path + "/BiliMiao/"
    var fileName = path

    init {
        if(miao) {
            isPath(path)
            path = isPath("$path$pathName/")
        }else{
            path = pathName
        }
    }

    @Throws(IOException::class)
    fun saveJPG(bitmap: Bitmap, bitName: String) : FileUtil {
        fileName = "$path$bitName.jpg"
        val f = File(fileName)
        val fOut = FileOutputStream(f)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
        fOut.flush()
        fOut.close()

        //发送至相册
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val uri = Uri.fromFile(f)
        intent.data = uri
        context?.sendBroadcast(intent)
        return this
    }

    fun saveText(data: ByteArray,name: String) : FileUtil{
        fileName = path + name
        val f = File(fileName)
        val fOut = FileOutputStream(f)
        fOut.write(data)
        fOut.flush()
        fOut.close()
        return this
    }

    fun <T> saveJSON(obj: T,fileName: String){
        var strJSON = Gson().toJson(obj)
        val f = File(path + fileName + ".json")
        val fOut = FileOutputStream(f)
        fOut.write(strJSON.toByteArray())
        fOut.flush()
        fOut.close()
    }

    fun <T> readJson(fileName: String,classOfT: Class<T>): T?{
        val f = File(path + fileName + ".json")
        if(!f.exists())
            return null
        val inputStream = FileInputStream(f)
        val bytes = ByteArray(1024)
        val arrayOutputStream = ByteArrayOutputStream()
        while (inputStream.read(bytes) !== -1) {
            arrayOutputStream.write(bytes, 0, bytes.size)
        }
        inputStream.close()
        arrayOutputStream.close()
        val strJSON = String(arrayOutputStream.toByteArray())
        var obj = Gson().fromJson(strJSON,classOfT)
        return obj
    }

    fun del(fileName: String){
        val f = File(path + fileName + ".json")
        if (f.exists()) {
            f.delete()
        }
    }

    companion object {
        fun isPath(path: String): String {
            val file = File(path)
            // 如果SD卡目录不存在创建
            if (!file.exists()) {
                file.mkdir()
            }
            return path
        }
    }
}
