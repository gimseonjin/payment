package com.kerry.payment.payment.presentation.view.request

import java.util.UUID

data class CheckoutRequest(
    val productIds: List<Long> = listOf(1L, 2L),
    var buyerId: Long = 1L,
    var seed: String = UUID.randomUUID().toString(),
)
