package com.kerry.payment.ledger.domain

import com.kerry.payment.wallets.domain.ReferenceType
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "ledger_transaction")
data class LedgerTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(nullable = false)
    var description: String,
    @Column(nullable = false)
    var referenceType: ReferenceType,
    @Column(nullable = false)
    var referenceId: Long,
    @Column(nullable = false)
    var orderId: String,
    @Column(nullable = false)
    var idempotencyKey: String?,
    @Column(nullable = false)
    var createdAt: LocalDateTime,
)
