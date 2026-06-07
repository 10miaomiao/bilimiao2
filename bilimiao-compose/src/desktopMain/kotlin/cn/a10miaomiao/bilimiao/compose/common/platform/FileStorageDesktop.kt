package cn.a10miaomiao.bilimiao.compose.common.platform

class FileStorageDesktop : FileStorage {

    override fun readText(fileName: String): String? {
        println("FileStorage.readText is not supported on Desktop: $fileName")
        return null
    }

    override fun writeText(fileName: String, content: String) {
        println("FileStorage.writeText is not supported on Desktop: $fileName")
    }

    override fun readBytes(fileName: String): ByteArray? {
        println("FileStorage.readBytes is not supported on Desktop: $fileName")
        return null
    }

    override fun writeBytes(fileName: String, content: ByteArray) {
        println("FileStorage.writeBytes is not supported on Desktop: $fileName")
    }
}
