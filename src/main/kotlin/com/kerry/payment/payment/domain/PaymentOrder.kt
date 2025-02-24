@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_order")
data class PaymentOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column(nullable = false)
    var sellerId: Long,
    @Column(nullable = false)
    var productId: Long,
    @Column(nullable = false)
    var orderId: String,
    @Column(nullable = false)
    var amount: Long,
    @Column(nullable = false)
    var paymentOrderStatus: PaymentStatus,
    @Column(nullable = false)
    private var isLedgerUpdated: Boolean,
    @Column(nullable = false)
    private var isWalletUpdated: Boolean,
    @Column(nullable = false)
    var failedCount: Int,
    @Column(nullable = false)
    var failedThreshold: Int,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    @Column(nullable = false)
    var updatedAt: LocalDateTime,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_event_id", nullable = false)
    lateinit var paymentEvent: PaymentEvent

    companion object {
        fun create(
            sellerId: Long,
            productId: Long,
            orderId: String,
            amount: Long,
            paymentOrderStatus: PaymentStatus,
        ): PaymentOrder =
            PaymentOrder(
                sellerId = sellerId,
                productId = productId,
                orderId = orderId,
                amount = amount,
                paymentOrderStatus = paymentOrderStatus,
                isLedgerUpdated = false,
                isWalletUpdated = false,
                failedCount = 0,
                failedThreshold = 5,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            )
    }

    fun isLedgerUpdated(): Boolean = isLedgerUpdated

    fun isWalletUpdated(): Boolean = isWalletUpdated
}
