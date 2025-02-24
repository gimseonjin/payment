package com.kerry.payment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

enum class PaymentStatus(description: String) {
    NOT_STARTED("결제 승인 시작 전"),
    EXECUTING("결제 승인 중"),
    SUCCESS("결제 승인 성공"),
    FAILURE("결제 승인 실패"),
    UNKNOWN("결제 승인 알 수 없는 상태");

    companion object {
        fun get(status: String): PaymentStatus {
            return entries.find { it.name == status } ?: throw IllegalArgumentException("PaymentStatus: $status 는 올바르지 않은 결제 타입입니다.")
        }
    }
}

@Entity
class PaymentOrderHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,
    @OneToOne
    var paymentOrder: PaymentOrder,
    @Column(nullable = false)
    var previousStatus: PaymentStatus,
    @Column(nullable = false)
    var newStatus: PaymentStatus,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
    @Column(nullable = false)
    var changedBy: String,
    @Column()
    var reason: String?,
) {
}