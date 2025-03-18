package com.kerry.payment.ledger.infra.executor

import com.kerry.payment.ledger.application.DoubleLedgerEntryRecordService
import com.kerry.payment.payment.domain.event.PaymentConfirmedEvent
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class LedgerTaskExecutor(
    private val doubleLedgerEntryRecordService: DoubleLedgerEntryRecordService,
) {
    @TransactionalEventListener
    fun execute(event: PaymentConfirmedEvent) {
        doubleLedgerEntryRecordService.recordDoubleLedgerEntry(
            paymentEventId = event.paymentEventId,
            orderId = event.orderId,
            paymentOrders = event.paymentOrders,
        )
    }
}
