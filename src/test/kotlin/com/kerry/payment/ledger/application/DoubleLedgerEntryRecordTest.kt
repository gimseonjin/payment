package com.kerry.payment.ledger.application

import com.kerry.payment.BaseTest
import com.kerry.payment.ledger.domain.LedgerRepository
import com.kerry.payment.ledger.domain.LedgerType
import com.kerry.payment.payment.domain.PaymentEvent
import com.kerry.payment.payment.domain.PaymentOrder
import com.kerry.payment.payment.domain.PaymentStatus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class DoubleLedgerEntryRecordTest : BaseTest() {
    @Autowired
    lateinit var doubleLedgerEntryRecordService: DoubleLedgerEntryRecordService

    @Autowired
    lateinit var ledgerRepository: LedgerRepository

    @Test
    fun `should record double ledger entries successfully`() {
        val paymentEventId = 1L
        val orderId = "order-1"
        val paymentOrders = preparePaymentOrders(orderId)

        doubleLedgerEntryRecordService.recordDoubleLedgerEntry(paymentEventId, orderId, paymentOrders)

        val ledgers = ledgerRepository.findAll()
        val sumOfAmount =
            ledgers.sumOf {
                when (it.type) {
                    LedgerType.CREDIT -> it.amount
                    LedgerType.DEBIT -> it.amount * -1
                }
            }

        assertEquals(0, sumOfAmount)
        assertEquals(4, ledgers.size)
    }

    private fun preparePaymentOrders(orderId: String): List<PaymentOrder> =
        listOf(
            PaymentOrder.create(
                sellerId = 1,
                productId = 1,
                orderId = orderId,
                amount = 3000,
                paymentOrderStatus = PaymentStatus.SUCCESS,
                paymentEvent =
                    PaymentEvent.create(
                        buyerId = 0,
                        orderId = orderId,
                        orderName = "order-1",
                    ),
            ),
            PaymentOrder.create(
                sellerId = 2,
                productId = 2,
                orderId = orderId,
                amount = 4000,
                paymentOrderStatus = PaymentStatus.SUCCESS,
                paymentEvent =
                    PaymentEvent.create(
                        buyerId = 0,
                        orderId = orderId,
                        orderName = "order-2",
                    ),
            ),
        )
}
