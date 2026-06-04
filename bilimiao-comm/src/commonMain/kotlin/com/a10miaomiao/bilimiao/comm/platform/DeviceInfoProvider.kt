package com.a10miaomiao.bilimiao.comm.platform

interface DeviceInfoProvider {
    val brand: String
    val model: String
    val osVersion: String
    val device: String
    val systemUserAgent: String
}
