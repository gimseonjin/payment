package com.kerry.payment.payment.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentOrderRepository : JpaRepository<PaymentOrder, Long> {
    fun findByOrderId(orderId: String): PaymentOrder?

    @Modifying(clearAutomatically = true)
    @Query(
        """
        UPDATE PaymentOrder p
        SET p.isLedgerUpdated = TRUE,
            p.updatedAt = CURRENT_TIMESTAMP
        WHERE p.orderId = :orderId
    """,
    )
    fun updateLedgerStatus(
        @Param("orderId") orderId: String,
    )

    @Modifying(clearAutomatically = true)
    @Query(
        """
        UPDATE PaymentOrder p
        SET p.isWalletUpdated = TRUE,
            p.updatedAt = CURRENT_TIMESTAMP
        WHERE p.orderId = :orderId
    """,
    )
    fun updateWalletStatus(
        @Param("orderId") orderId: String,
    )
}
