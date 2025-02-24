package com.kerry.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

enum class PaymentType(description: String) {
    NORMAL("일반 결제");

    companion object {
        fun get(type: String): PaymentType {
            return entries.find { it.name == type } ?: error("PaymentType (type: $type) 은 올바르지 않은 결제 타입입니다.")
        }
    }
}

enum class PaymentMethod(val method: String) {
    EASY_PAY("간편결제");

    companion object {
        fun get(method: String): PaymentMethod {
            return entries.find { it.method == method } ?: error("Payment Method (methpd: $method) 는 올바르이 않은 결제 방법입니다.")
        }
    }
}

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
    var approvedAt: LocalDateTime?
) {
    @OneToMany(mappedBy = "paymentEvent", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    lateinit var orders: MutableList<PaymentOrder>

    companion object {
        fun create(
            buyerId: Long,
            orderId: String,
            orderName: String,
        ): PaymentEvent {
            return PaymentEvent(
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
                approvedAt = null
            )
        }
    }

    fun addOrders(orders: List<PaymentOrder>) {
        if (!this::orders.isInitialized) {
            this.orders = mutableListOf()
        }
        this.orders.addAll(orders)
        orders.forEach { it.paymentEvent = this }
    }

    fun totalAmount(): Long {
        return orders.sumOf { it.amount }
    }
}