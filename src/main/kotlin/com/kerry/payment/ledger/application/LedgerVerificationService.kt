package com.kerry.payment.ledger.application

import com.kerry.payment.ledger.domain.LedgerDiscrepancy
import com.kerry.payment.ledger.domain.LedgerRepository
import com.kerry.payment.ledger.domain.LedgerVerificationNotifier
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class LedgerVerificationService(
    private val ledgerRepository: LedgerRepository,
    private val ledgerVerificationNotifier: LedgerVerificationNotifier,
) {
    @Transactional(readOnly = true)
    fun verifyLedger(
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<LedgerDiscrepancy> {
        val discrepancies = ledgerRepository.findDiscrepancies(from, to)

        if (discrepancies.isNotEmpty()) {
            print(
                "🚨 Ledger Discrepancy Detected!\n" +
                    discrepancies.joinToString("\n") {
                        "❌ ${it.transactionDate}: " +
                            "Credit ${it.totalCredit} ≠ Debit ${it.totalDebit} (Diff: ${it.totalCredit - it.totalDebit})"
                    },
            )
            ledgerVerificationNotifier.notifyDiscrepancy(
                "🚨 Ledger Discrepancy Detected!\n" +
                    discrepancies.joinToString("\n") {
                        "❌ ${it.transactionDate}: " +
                            "Credit ${it.totalCredit} ≠ Debit ${it.totalDebit} (Diff: ${it.totalCredit - it.totalDebit})"
                    },
            )
        }

        return discrepancies
    }
}
