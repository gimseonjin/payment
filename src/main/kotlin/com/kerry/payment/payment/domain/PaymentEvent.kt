@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_event")
data class PaymentEvent(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
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
                id = null,
            )
    }

    fun addOrders(orders: List<PaymentOrder>) {
        if (!this::orders.isInitialized) {
            this.orders = mutableListOf()
        }
        this.orders.addAll(orders)
        orders.forEach {
            if (it.paymentEvent != this) {
                it.paymentEvent = this
            }
        }
    }

    fun totalAmount(): Long = orders.sumOf { it.amount }

    fun updateStatusToExecuting() {
        orders.forEach { it.updateStatusToExecuting() }
    }

    fun updateStatusToSuccess() {
        orders.forEach {
            it.updateStatusToSuccess()
        }
    }

    fun updateStatusToFailure(reason: String) {
        orders.forEach {
            it.updateStatusToFailure(reason)
        }
    }

    fun updateStatusToUnknown() {
        orders.forEach {
            it.updateStatusToUnknown()
        }
    }

    fun isValid(amount: Long): Boolean = amount == totalAmount()

    fun updateStatus(
        paymentKey: String,
        orderId: String,
        status: PaymentStatus,
        extraDetails: PaymentExtraDetails?,
        failure: PaymentFailure?,
    ) {
        require(status == PaymentStatus.SUCCESS || status == PaymentStatus.FAILURE || status == PaymentStatus.UNKNOWN) {
            "결제 (orderId: $orderId) 는 올바르지 않은 결제 상태입니다."
        }

        if (status == PaymentStatus.SUCCESS) {
            requireNotNull(extraDetails) {
                "결제 성공시에는 extraDetails 는 필수입니다."
            }
        }

        if (status == PaymentStatus.FAILURE) {
            requireNotNull(failure) {
                "결제 실패시에는 failure 는 필수입니다."
            }
        }

        this.paymentKey = paymentKey

        when (status) {
            PaymentStatus.SUCCESS -> {
                this.type = extraDetails!!.type
                this.method = extraDetails.method
                this.approvedAt = extraDetails.approvedAt
                this.pspRawData = extraDetails.pspRawData
                this.orderName = extraDetails.orderName
                this.updateStatusToSuccess()
            }
            PaymentStatus.FAILURE -> this.updateStatusToFailure(failure.toString())
            else -> this.updateStatusToUnknown()
        }
    }
}
