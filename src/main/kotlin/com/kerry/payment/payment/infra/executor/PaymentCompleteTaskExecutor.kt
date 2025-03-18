package com.kerry.payment.payment.infra.executor

import com.kerry.payment.ledger.domain.event.LedgerEventMessage
import com.kerry.payment.payment.application.PaymentCompleteService
import com.kerry.payment.wallets.domain.event.WalletEventMessage
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class PaymentCompleteTaskExecutor(
    private val paymentCompleteService: PaymentCompleteService,
) {
    @TransactionalEventListener
    fun completePayment(event: WalletEventMessage) {
        paymentCompleteService.markWalletAsUpdated(event.orderId)
    }

    @TransactionalEventListener
    fun completePayment(event: LedgerEventMessage) {
        paymentCompleteService.markLedgerAsUpdated(event.orderId)
    }
}
