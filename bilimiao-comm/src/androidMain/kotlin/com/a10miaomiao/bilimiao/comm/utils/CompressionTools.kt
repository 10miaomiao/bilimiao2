package com.a10miaomiao.bilimiao.comm.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater

actual object CompressionTools {

    actual fun compress(value: ByteArray, offset: Int, length: Int, compressionLevel: Int): ByteArray {
        val bos = ByteArrayOutputStream(length)
        val compressor = Deflater()
        try {
            compressor.setLevel(compressionLevel)
            compressor.setInput(value, offset, length)
            compressor.finish()
            val buf = ByteArray(1024)
            while (!compressor.finished()) {
                val count = compressor.deflate(buf)
                bos.write(buf, 0, count)
            }
        } finally {
            compressor.end()
        }
        return bos.toByteArray()
    }

    actual fun compress(value: ByteArray, offset: Int, length: Int): ByteArray {
        return compress(value, offset, length, Deflater.BEST_COMPRESSION)
    }

    actual fun compress(value: ByteArray): ByteArray {
        return compress(value, 0, value.size, Deflater.BEST_COMPRESSION)
    }

    actual fun decompress(value: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream(value.size)
        val decompressor = Inflater()
        try {
            decompressor.setInput(value)
            val buf = ByteArray(1024)
            while (!decompressor.finished()) {
                val count = decompressor.inflate(buf)
                bos.write(buf, 0, count)
            }
        } finally {
            decompressor.end()
        }
        return bos.toByteArray()
    }

    actual fun decompressXML(data: ByteArray): ByteArray {
        var data = data
        val dest = ByteArray(data.size + 2)
        System.arraycopy(data, 0, dest, 2, data.size)
        dest[0] = 0x78
        dest[1] = 0x01
        data = dest
        val decompresser = Inflater()
        decompresser.setInput(data)
        val bufferArray = ByteArray(1024)
        val baos = ByteArrayOutputStream(1024)
        try {
            var i = 1
            while (i != 0) {
                i = decompresser.inflate(bufferArray)
                baos.write(bufferArray, 0, i)
            }
            data = baos.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                baos.flush()
                baos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        decompresser.end()
        return data
    }
}
