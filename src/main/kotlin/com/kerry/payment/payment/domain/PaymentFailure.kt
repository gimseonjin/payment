package com.kerry.payment.payment.domain

data class PaymentFailure(
    val errorCode: String,
    val message: String,
)
