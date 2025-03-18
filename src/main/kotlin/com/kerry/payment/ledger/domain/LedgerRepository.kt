package com.kerry.payment.ledger.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface LedgerRepository : JpaRepository<Ledger, Long> {
    @Query(
        """
        SELECT FUNCTION('DATE', l.createdAt) AS transactionDate,
               SUM(CASE WHEN l.type = 'CREDIT' THEN l.amount ELSE 0 END) AS totalCredit,
               SUM(CASE WHEN l.type = 'DEBIT' THEN l.amount ELSE 0 END) AS totalDebit
        FROM Ledger l
        WHERE l.createdAt BETWEEN :from AND :to
        GROUP BY FUNCTION('DATE', l.createdAt)
        HAVING SUM(CASE WHEN l.type = 'CREDIT' THEN l.amount ELSE 0 END) !=
               SUM(CASE WHEN l.type = 'DEBIT' THEN l.amount ELSE 0 END)
    """,
    )
    fun findDiscrepancies(
        @Param("from") from: LocalDateTime,
        @Param("to") to: LocalDateTime,
    ): List<LedgerDiscrepancy>
}
