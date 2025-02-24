package com.kerry.payment.payment.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentEventRepository : JpaRepository<PaymentEvent, Long> {
    fun save(paymentEvent: PaymentEvent): PaymentEvent

    fun findByOrderId(orderId: String): PaymentEvent?
}
