package com.a10miaomiao.bilimiao.comm.scanner

import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Point
import android.os.Parcel
import android.os.Parcelable.Creator
import androidx.activity.result.ActivityResult
import com.a10miaomiao.bilimiao.comm.utils.miaoLogger
import org.microg.safeparcel.AutoSafeParcelable
import org.microg.safeparcel.SafeParcelReader
import org.microg.safeparcel.SafeParcelUtil
import org.microg.safeparcel.SafeParcelable


object BilimiaoScanner {

    const val REQUEST_CODE = 10001
    var resultCallback: ((result: String) -> Unit)? = null

    fun openScanner(
        activity: Activity,
        themeColor: Int,
        callback: (result: String) -> Unit,
    ): Boolean {
        resultCallback = callback
        val applicationInfo: ApplicationInfo = activity.applicationInfo
        val labelRes = applicationInfo.labelRes
        val labelName = if (labelRes != 0) {
            activity.getString(labelRes)
        } else {
            applicationInfo.loadLabel(activity.packageManager).toString()
        }
        // 优先启动bilimiao扫码器
        kotlin.runCatching {
            val intent = Intent()
            intent.setAction("bilimiao.plugin.ACTION_SCAN_QRCODE") // 与插件Manifest中定义的Action一致
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            intent.putExtra("extra_theme_color", themeColor)
                .putExtra("extra_calling_app_name", labelName)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }.let {
            if (it.isSuccess) {
                // 启动bilimiao扫码器成功
                return true
            }
        }
        // 再尝试启动GMS扫码器
        kotlin.runCatching {
            val intent = Intent()
                .setPackage("com.google.android.gms")
                .setAction("com.google.android.gms.mlkit.ACTION_SCAN_BARCODE")
                .putExtra("extra_calling_app_name", labelName)
                .putExtra("extra_supported_formats", 0)
                .putExtra("extra_allow_manual_input", false)
                .putExtra("extra_enable_auto_zoom", false)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }.let {
            if (it.isSuccess) {
                // 启动GMS扫码器成功
                return true
            }
        }
        // 启动失败
        // 提示下载bilimiao扫码器
        resultCallback = null
        return false
    }

    fun onActivityResult(
        result: ActivityResult
    ) {
        val data: Intent = result.data ?: return
        if (result.resultCode == Activity.RESULT_OK) {
            if (data.hasExtra("extra_barcode_result")) {
                // GMS扫码器扫码结果
                val barcodeBytes = data.getByteArrayExtra("extra_barcode_result") ?: return
                val barcodeResult = SafeParcelUtil.fromByteArray(barcodeBytes, GmsBarcodeResult.CREATOR)
                val textResult = barcodeResult.displayValue
                resultCallback?.invoke(textResult)
                resultCallback = null
            } else if (data.hasExtra("extra_qrcode_text_result")) {
                // bilimiao扫码器扫码结果
                val textResult = data.getStringExtra("extra_qrcode_text_result") ?: return
                resultCallback?.invoke(textResult)
                resultCallback = null
            }
        }
    }

}
