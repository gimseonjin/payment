package com.kerry.payment.ledger.domain

interface LedgerVerificationNotifier {
    fun notifyDiscrepancy(message: String)
}
