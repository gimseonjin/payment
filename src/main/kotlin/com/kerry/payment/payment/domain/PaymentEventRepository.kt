package com.kerry.payment.payment.domain

import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PaymentEventRepository : JpaRepository<PaymentEvent, Long> {
    fun save(paymentEvent: PaymentEvent): PaymentEvent

    fun findByOrderId(orderId: String): PaymentEvent?

    @Query(
        """
        SELECT pe
        FROM PaymentEvent pe
        INNER JOIN PaymentOrder po ON pe.orderId = po.orderId
        WHERE 
            po.paymentOrderStatus = 'UNKNOWN' 
            OR (po.paymentOrderStatus = 'EXECUTING' 
                AND po.updatedAt BETWEEN :from AND :to)
            AND po.failedCount < po.failedThreshold
      """,
    )
    fun findRetryablePayments(
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime,
        pageable: Pageable,
    ): List<PaymentEvent>
}
