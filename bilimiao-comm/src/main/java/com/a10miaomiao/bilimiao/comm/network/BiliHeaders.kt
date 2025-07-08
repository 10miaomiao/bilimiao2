package com.a10miaomiao.bilimiao.comm.network

object BiliHeaders {
    const val Bearer = "Bearer";
    const val Identify = "identify_v1";
    const val FormUrlEncodedContentType = "application/x-www-form-urlencoded";
    const val JsonContentType = "application/json";
    const val GRPCContentType = "application/grpc";
    const val UserAgent = "User-Agent";
    const val Referer = "Referer";
    const val AppKey = "APP-KEY";
    const val RequestedWith = "X-Requested-With"
    const val BiliMeta = "x-bili-metadata-bin";
    const val Authorization = "authorization";
    const val BiliDevice = "x-bili-device-bin";
    const val BiliNetwork = "x-bili-network-bin";
    const val BiliRestriction = "x-bili-restriction-bin";
    const val BiliLocale = "x-bili-locale-bin";
    const val BiliFawkes = "x-bili-fawkes-req-bin";
    const val BiliMid = "x-bili-mid";
    const val GRPCAcceptEncodingKey = "grpc-accept-encoding";
    const val GRPCAcceptEncodingValue = "identity,deflate,gzip";
    const val GRPCTimeOutKey = "grpc-timeout";
    const val GRPCTimeOutValue = "20100m";
    const val Envoriment = "env";
    const val TransferEncodingKey = "Transfer-Encoding";
    const val TransferEncodingValue = "chunked";
    const val TEKey = "TE";
    const val TEValue = "trailers";
    const val Buvid = "buvid"

    // content-encoding
    const val GRPCEncoding = "grpc-encoding"
    const val GRPCEncodingGZIP = "gzip"
}