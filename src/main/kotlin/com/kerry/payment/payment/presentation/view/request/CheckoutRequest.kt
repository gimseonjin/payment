package com.kerry.payment.payment.presentation.view.request

data class CheckoutRequest(
    val productIds: List<Long>,
    var buyerId: Long,
    var seed: String,
)