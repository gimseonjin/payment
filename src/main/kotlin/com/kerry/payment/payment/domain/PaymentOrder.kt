@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment_order")
data class PaymentOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(nullable = false)
    var sellerId: Long,
    @Column(nullable = false)
    var productId: Long,
    @Column(nullable = false)
    var orderId: String,
    @Column(nullable = false)
    var amount: Long,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_event_id", nullable = false)
    var paymentEvent: PaymentEvent,
) {
    @OneToMany(mappedBy = "paymentOrder", cascade = [CascadeType.ALL], orphanRemoval = true)
    lateinit var histories: MutableList<PaymentOrderHistory>

    companion object {
        fun create(
            sellerId: Long,
            productId: Long,
            orderId: String,
            amount: Long,
            paymentOrderStatus: PaymentStatus,
            paymentEvent: PaymentEvent,
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
                paymentEvent = paymentEvent,
                id = null,
            )
    }

    fun isLedgerUpdated(): Boolean = isLedgerUpdated

    fun isWalletUpdated(): Boolean = isWalletUpdated

    fun updateStatusToExecuting() {
        require(paymentOrderStatus != PaymentStatus.SUCCESS || paymentOrderStatus != PaymentStatus.FAILURE) {
            "이미 처리가 완료된 주문입니다."
        }

        paymentOrderStatus = PaymentStatus.EXECUTING
        updatedAt = LocalDateTime.now()

        if (!this::histories.isInitialized) {
            this.histories = mutableListOf()
        }
        histories.add(
            PaymentOrderHistory.create(
                previousStatus = PaymentStatus.NOT_STARTED,
                newStatus = PaymentStatus.EXECUTING,
                changedBy = null,
                reason = null,
                paymentOrder = this,
            ),
        )
    }

    fun updateStatusToSuccess() {
        require(paymentOrderStatus != PaymentStatus.SUCCESS || paymentOrderStatus != PaymentStatus.FAILURE) {
            "이미 처리가 완료된 주문입니다."
        }

        paymentOrderStatus = PaymentStatus.SUCCESS
        updatedAt = LocalDateTime.now()

        if (!this::histories.isInitialized) {
            this.histories = mutableListOf()
        }
        histories.add(
            PaymentOrderHistory.create(
                previousStatus = PaymentStatus.EXECUTING,
                newStatus = PaymentStatus.SUCCESS,
                changedBy = null,
                reason = "PAYMENT_CONFIRMATION_DONE",
                paymentOrder = this,
            ),
        )
    }

    fun updateStatusToFailure(reason: String) {
        require(paymentOrderStatus != PaymentStatus.SUCCESS || paymentOrderStatus != PaymentStatus.FAILURE) {
            "이미 처리가 완료된 주문입니다."
        }

        paymentOrderStatus = PaymentStatus.FAILURE
        updatedAt = LocalDateTime.now()

        if (!this::histories.isInitialized) {
            this.histories = mutableListOf()
        }
        histories.add(
            PaymentOrderHistory.create(
                previousStatus = PaymentStatus.EXECUTING,
                newStatus = PaymentStatus.FAILURE,
                changedBy = null,
                reason = reason,
                paymentOrder = this,
            ),
        )
    }

    fun updateStatusToUnknown() {
        require(paymentOrderStatus != PaymentStatus.SUCCESS || paymentOrderStatus != PaymentStatus.FAILURE) {
            "이미 처리가 완료된 주문입니다."
        }

        failedCount++
        paymentOrderStatus = PaymentStatus.UNKNOWN
        updatedAt = LocalDateTime.now()

        if (!this::histories.isInitialized) {
            this.histories = mutableListOf()
        }
        histories.add(
            PaymentOrderHistory.create(
                previousStatus = PaymentStatus.EXECUTING,
                newStatus = PaymentStatus.FAILURE,
                changedBy = null,
                reason = "UNKNOWN",
                paymentOrder = this,
            ),
        )
    }
}
