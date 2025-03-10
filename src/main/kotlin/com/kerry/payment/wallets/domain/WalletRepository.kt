package com.kerry.payment.wallets.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface WalletRepository : JpaRepository<Wallet, Long> {
    @Query("SELECT w FROM Wallet w WHERE w.userId IN :sellerIds")
    fun getWalletsBySellerIds(sellerIds: Set<Long>): List<Wallet>
}
