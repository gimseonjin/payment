package com.kerry.payment.payment.domain.event

import com.kerry.payment.payment.domain.PaymentOrder

data class PaymentConfirmedEvent(
    val paymentEventId: Long,
    val orderId: String,
    val paymentOrders: List<PaymentOrder>,
)
