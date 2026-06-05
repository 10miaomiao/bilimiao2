package com.a10miaomiao.bilimiao.comm.utils

expect object CompressionTools {
    fun compress(value: ByteArray, offset: Int, length: Int, compressionLevel: Int): ByteArray
    fun compress(value: ByteArray, offset: Int, length: Int): ByteArray
    fun compress(value: ByteArray): ByteArray
    fun decompress(value: ByteArray): ByteArray
    fun decompressXML(data: ByteArray): ByteArray
}
