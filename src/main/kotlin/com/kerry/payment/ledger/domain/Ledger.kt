package com.kerry.payment.ledger.domain

import com.kerry.payment.common.IdempotencyCreator
import com.kerry.payment.wallets.domain.ReferenceType
import jakarta.persistence.*
import java.time.LocalDateTime

enum class LedgerType {
    CREDIT,
    DEBIT,
}

data class DoubleLedgerEntry(
    val credit: Ledger,
    val debit: Ledger,
)

@Entity
@Table(name = "ledger")
data class Ledger(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long?,
    @Column(nullable = false)
    val amount: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: LedgerType,
    @Column(nullable = false)
    val accountId: Long,
    @ManyToOne(fetch = FetchType.LAZY, cascade = [CascadeType.PERSIST])
    @JoinColumn(name = "transaction_id", nullable = false)
    val transaction: LedgerTransaction?,
    @Column(nullable = false)
    val createdAt: LocalDateTime,
) {
    companion object {
        fun createDoubleLedgerEntry(
            sellerAccountId: Long,
            buyerAccountId: Long,
            amount: Long,
            referenceType: ReferenceType,
            referenceId: Long,
            orderId: String,
        ): DoubleLedgerEntry {
            val transaction =
                LedgerTransaction(
                    id = null,
                    referenceType = referenceType,
                    referenceId = referenceId,
                    orderId = orderId,
                    description = "LedgerService record transaction",
                    idempotencyKey = null,
                    createdAt = LocalDateTime.now(),
                ).apply {
                    idempotencyKey = IdempotencyCreator.create(this)
                }

            return DoubleLedgerEntry(
                credit =
                    Ledger(
                        id = null,
                        accountId = sellerAccountId,
                        amount = amount,
                        type = LedgerType.CREDIT,
                        transaction = transaction,
                        createdAt = LocalDateTime.now(),
                    ).apply { },
                debit =
                    Ledger(
                        id = null,
                        accountId = buyerAccountId,
                        amount = amount,
                        transaction = transaction,
                        createdAt = LocalDateTime.now(),
                        type = LedgerType.DEBIT,
                    ),
            )
        }
    }
}
