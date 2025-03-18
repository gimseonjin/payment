package com.kerry.payment.ledger.domain

import org.springframework.data.jpa.repository.JpaRepository

interface LedgerTransactionRepository : JpaRepository<LedgerTransaction, Long> {
    fun existsByOrderId(orderId: String): Boolean
}
