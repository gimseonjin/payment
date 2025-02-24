package com.kerry.payment.payment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import java.time.LocalDateTime

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