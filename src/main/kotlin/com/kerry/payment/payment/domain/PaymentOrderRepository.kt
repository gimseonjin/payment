package com.kerry.payment.payment.domain

import org.springframework.data.jpa.repository.JpaRepository

interface PaymentOrderRepository : JpaRepository<PaymentOrder, Long> {
    fun findByOrderId(orderId: String): PaymentOrder?
}
