package cn.a10miaomiao.download

import java.io.*

object FileUtil {

    /**
     * 创建文件
     * @param filePath 文件路径(不要以/结尾)
     * @param fileName 文件名称（包含后缀,如：ReadMe.txt）
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createTxtFile(filePath: String, fileName: String): Boolean {
        var flag = false
        val filename = File("$filePath/$fileName")
        if (!filename.exists()) {
            filename.createNewFile()
            flag = true
        }
        return flag
    }


    fun writeTxtFile(file: File, content: String, append: Boolean): Boolean {
        var flag = true
        try {
            val fw = FileWriter(file.path, append)
            fw.write(content)
            fw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return flag
    }

    @Throws(Exception::class)
    fun readTxtFile(file: File): String? {
        var result: String? = ""
        var fileReader: FileReader? = null
        var bufferedReader: BufferedReader? = null
        try {
            fileReader = FileReader(file)
            bufferedReader = BufferedReader(fileReader)
            try {
                var read: String? = null
                while ({ read = bufferedReader.readLine();read }() != null) {
                    result = result + read + "\r\n"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
            fileReader?.close()
        }
        return result
    }

    @Throws(IOException::class)
    fun inputStreamToFile(inputStream: InputStream, file: File) {
        val outputStream = FileOutputStream(file)
        var read = -1
        inputStream.use { input ->
            outputStream.use {
                while (input.read().also { read = it } != -1) {
                    it.write(read)
                }
            }
        }
//        var bytesWritten = 0
//        var byteCount = 0
//        val bytes = ByteArray(1024)
//        while (inputStream.read(bytes).also { byteCount = it } != -1) {
//            outputStream.write(bytes, bytesWritten, byteCount)
//            bytesWritten += byteCount
//        }
//        inputStream.close()
//        outputStream.close()
    }
}