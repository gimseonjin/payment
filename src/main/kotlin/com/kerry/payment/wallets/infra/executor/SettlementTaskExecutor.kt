package com.kerry.payment.wallets.infra.executor

import com.kerry.payment.payment.domain.event.PaymentConfirmedEvent
import com.kerry.payment.wallets.application.SettlementService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class SettlementTaskExecutor(
    private val settlementService: SettlementService,
) {
    @TransactionalEventListener
    fun execute(event: PaymentConfirmedEvent) {
        settlementService.processSettlement(
            paymentEventId = event.paymentEventId,
            orderId = event.orderId,
            paymentOrders = event.paymentOrders,
        )
    }
}
