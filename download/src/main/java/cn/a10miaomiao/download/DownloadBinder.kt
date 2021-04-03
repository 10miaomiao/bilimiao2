package cn.a10miaomiao.download

import android.os.Binder

class DownloadBinder(
        var downloadService: DownloadService
) : Binder()