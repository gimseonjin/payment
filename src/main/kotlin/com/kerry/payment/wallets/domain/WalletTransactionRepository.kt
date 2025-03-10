package com.kerry.payment.wallets.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WalletTransactionRepository : JpaRepository<WalletTransaction, Long> {
    fun existsByOrderId(orderId: String): Boolean
}
