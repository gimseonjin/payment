package com.kerry.payment.ledger.application

import com.kerry.payment.ledger.domain.Ledger
import com.kerry.payment.ledger.domain.LedgerRepository
import com.kerry.payment.ledger.domain.LedgerTransactionRepository
import com.kerry.payment.ledger.domain.event.LedgerEventMessage
import com.kerry.payment.ledger.domain.event.LedgerEventMessageType
import com.kerry.payment.payment.domain.PaymentOrder
import com.kerry.payment.wallets.domain.ReferenceType
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DoubleLedgerEntryRecordService(
    private val ledgerTransactionRepository: LedgerTransactionRepository,
    private val ledgerRepository: LedgerRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun recordDoubleLedgerEntry(
        paymentEventId: Long,
        orderId: String,
        paymentOrders: List<PaymentOrder>,
    ) {
        if (ledgerTransactionRepository.existsByOrderId(orderId)) {
            return
        }

        val ledgerEntries =
            paymentOrders.map { paymentOrder ->
                val ledgerAccountPair = paymentOrder.getBuyerSellerAccountIds()

                Ledger.createDoubleLedgerEntry(
                    sellerAccountId = ledgerAccountPair.sellerId,
                    buyerAccountId = ledgerAccountPair.buyerId,
                    amount = paymentOrder.amount,
                    orderId = orderId,
                    referenceType = ReferenceType.PAYMENT,
                    referenceId = paymentEventId,
                )
            }

        ledgerRepository.saveAll(ledgerEntries.flatMap { listOf(it.credit, it.debit) })
        applicationEventPublisher.publishEvent(
            LedgerEventMessage(
                type = LedgerEventMessageType.SUCCESS,
                orderId = orderId,
            ),
        )
    }
}
