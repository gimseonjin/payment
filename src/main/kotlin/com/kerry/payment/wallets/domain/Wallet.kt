package com.kerry.payment.wallets.domain

import jakarta.persistence.*

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?,
    var userId: Long,
    var balance: Long,
    var createdAt: Long,
    var updatedAt: Long,
    @Version
    var version: Long = 0,
) {
    @OneToMany(mappedBy = "wallet", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    lateinit var transactions: MutableList<WalletTransaction>

    companion object {
        fun create(
            userId: Long,
            balance: Long = 0,
        ): Wallet =
            Wallet(
                id = null,
                userId = userId,
                balance = balance,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
            )
    }

    fun deposit(
        amount: Long,
        referenceType: ReferenceType,
        referenceId: Long,
        orderId: String,
    ) {
        require(amount > 0) { "Amount must be greater than 0" }

        if (!this::transactions.isInitialized) {
            this.transactions = mutableListOf()
        }

        balance += amount
        transactions.add(
            WalletTransaction.create(
                amount = amount,
                type = WalletTransactionType.DEPOSIT,
                referenceType = referenceType,
                referenceId = referenceId,
                orderId = orderId,
                wallet = this,
            ),
        )
    }
}
