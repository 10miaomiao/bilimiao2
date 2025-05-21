package com.a10miaomiao.bilimiao.comm.entity.home

import kotlinx.serialization.Serializable

@Serializable
data class RecommendCardArgsInfo (
    val up_id: String? = null,
    val up_name: String? = null,
    val rid: Int = 0,
    val rname: String? = null,
    val tid: Long = 0L,
    val tname: String? = null,
    val aid: String? = null,
)