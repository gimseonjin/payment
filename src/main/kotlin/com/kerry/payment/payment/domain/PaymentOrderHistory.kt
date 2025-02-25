@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class PaymentOrderHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(nullable = false)
    var previousStatus: PaymentStatus,
    @Column(nullable = false)
    var newStatus: PaymentStatus,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    @Column(nullable = true)
    var changedBy: String?,
    @Column(nullable = true)
    var reason: String?,
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_order_id", nullable = false)
    var paymentOrder: PaymentOrder,
) {
    companion object {
        fun create(
            previousStatus: PaymentStatus,
            newStatus: PaymentStatus,
            changedBy: String?,
            reason: String?,
            paymentOrder: PaymentOrder,
        ): PaymentOrderHistory =
            PaymentOrderHistory(
                previousStatus = previousStatus,
                newStatus = newStatus,
                createdAt = LocalDateTime.now(),
                changedBy = changedBy,
                reason = reason,
                paymentOrder = paymentOrder,
                id = null,
            )
    }
}
