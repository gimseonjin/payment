package com.kerry.payment.ledger.domain

import java.time.LocalDate

interface LedgerDiscrepancy {
    val transactionDate: LocalDate
    val totalCredit: Long
    val totalDebit: Long
}
