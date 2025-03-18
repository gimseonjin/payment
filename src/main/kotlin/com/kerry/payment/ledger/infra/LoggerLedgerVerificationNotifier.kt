package com.kerry.payment.ledger.infra

import com.kerry.payment.ledger.domain.LedgerVerificationNotifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LoggerLedgerVerificationNotifier : LedgerVerificationNotifier {
    private val logger: Logger = LoggerFactory.getLogger(LoggerLedgerVerificationNotifier::class.java)

    override fun notifyDiscrepancy(message: String) {
        logger.warn("🔔 테스트용 Ledger Verification Alert: $message")
    }
}
