@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_event")
data class PaymentEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L,
    @Column(nullable = false)
    var buyerId: Long,
    @Column(nullable = false)
    var orderId: String,
    @Column(nullable = false)
    var isPaymentDone: Boolean,
    @Column(unique = true)
    var paymentKey: String?,
    @Enumerated(EnumType.STRING)
    @Column
    var type: PaymentType?,
    @Column(nullable = false)
    var orderName: String,
    @Enumerated(EnumType.STRING)
    @Column
    var method: PaymentMethod?,
    @Column(columnDefinition = "TEXT")
    var pspRawData: String?,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    @Column(nullable = false)
    var updatedAt: LocalDateTime,
    @Column
    var approvedAt: LocalDateTime?,
) {
    @OneToMany(mappedBy = "paymentEvent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    lateinit var orders: MutableList<PaymentOrder>

    companion object {
        fun create(
            buyerId: Long,
            orderId: String,
            orderName: String,
        ): PaymentEvent =
            PaymentEvent(
                buyerId = buyerId,
                orderId = orderId,
                paymentKey = null,
                isPaymentDone = false,
                type = null,
                orderName = orderName,
                method = null,
                pspRawData = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
                approvedAt = null,
            )
    }

    fun addOrders(orders: List<PaymentOrder>) {
        if (!this::orders.isInitialized) {
            this.orders = mutableListOf()
        }
        this.orders.addAll(orders)
        orders.forEach { it.paymentEvent = this }
    }

    fun totalAmount(): Long = orders.sumOf { it.amount }
}
