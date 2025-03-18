package com.kerry.payment.wallets.infra.executor

import com.kerry.payment.payment.domain.event.PaymentConfirmedEvent
import com.kerry.payment.wallets.application.SettlementService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SettlementTaskExecutor(
    private val settlementService: SettlementService,
) {
    @EventListener
    fun execute(event: PaymentConfirmedEvent) {
        settlementService.processSettlement(
            paymentEventId = event.paymentEventId,
            orderId = event.orderId,
            paymentOrders = event.paymentOrders,
        )
    }
}
