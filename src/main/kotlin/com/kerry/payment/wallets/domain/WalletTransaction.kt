package com.kerry.payment.wallets.domain

import com.kerry.payment.common.IdempotencyCreator
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "wallet_transaction")
data class WalletTransaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    @Column(nullable = false)
    var amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: WalletTransactionType,
    @Enumerated(EnumType.STRING)
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
    @Column(nullable = false)
    var updatedAt: LocalDateTime,
) {
    @ManyToOne(fetch = FetchType.LAZY)
    lateinit var wallet: Wallet

    companion object {
        fun create(
            wallet: Wallet,
            amount: Long,
            type: WalletTransactionType,
            referenceType: ReferenceType,
            referenceId: Long,
            orderId: String,
        ): WalletTransaction =
            WalletTransaction(
                id = null,
                amount = amount,
                type = type,
                referenceType = referenceType,
                referenceId = referenceId,
                orderId = orderId,
                idempotencyKey = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now(),
            ).apply {
                this.wallet = wallet
                this.idempotencyKey = IdempotencyCreator.create(this)
            }
    }
}
