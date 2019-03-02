package com.a10miaomiao.bilimiao.entity

data class Payment(
        val dialog: Dialog,
        val pay_tip: PayTip,
        val pay_type: PayType,
        val price: String,
        val vip_promotion: String
)