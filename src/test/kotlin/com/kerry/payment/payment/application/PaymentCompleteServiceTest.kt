package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.test.Test

class PaymentCompleteServiceTest : BaseServiceTest() {
    @Autowired
    lateinit var paymentCompleteService: PaymentCompleteService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var paymentEventRepository: PaymentEventRepository

    @Test
    fun `should update payment given a WalletEventMessage`() {
        // given
        val orderId = "order-1"
        val buyerId = 1L
        val orderName = "order_name"
        prepareProductEvent(buyerId, orderId, orderName)

        // when
        paymentCompleteService.markWalletAsUpdated(orderId)

        // then - 지갑에만 업데이트되었고 결제는 아직 안됨
        val payment = paymentEventRepository.findByOrderId(orderId)
        assertNotNull(payment!!)
        assertTrue(payment.isWalletUpdateDone())
        assertFalse(payment.isPaymentDone)
    }

    @Test
    fun `should update payment given a LedgerEventMessage`() {
        // given
        val orderId = "order-1"
        val buyerId = 1L
        val orderName = "order_name"
        prepareProductEvent(buyerId, orderId, orderName)

        // when
        paymentCompleteService.markLedgerAsUpdated(orderId)

        // then - 원장에만 업데이트되었고 결제는 아직 안됨
        val payment = paymentEventRepository.findByOrderId(orderId)
        assertNotNull(payment!!)
        assertTrue(payment.isLedgerUpdateDone())
        assertFalse(payment.isPaymentDone)
    }

    @Test
    fun `should update payment given a WalletEventMessage and LedgerEventMessage`() {
        // given
        val orderId = "order-1"
        val buyerId = 1L
        val orderName = "order_name"
        prepareProductEvent(buyerId, orderId, orderName)

        // when
        paymentCompleteService.markWalletAsUpdated(orderId)
        paymentCompleteService.markLedgerAsUpdated(orderId)

        // then - 지갑과 원장 모두 업데이트되었고 결제가 완료됨
        val payment = paymentEventRepository.findByOrderId(orderId)
        assertNotNull(payment!!)
        assertTrue(payment.isWalletUpdateDone())
        assertTrue(payment.isLedgerUpdateDone())
        assertTrue(payment.isPaymentDone)
    }

    @Test
    fun `should update payment given a LedgerEventMessage and WalletEventMessage`() {
        // given
        val orderId = "order-1"
        val buyerId = 1L
        val orderName = "order_name"
        prepareProductEvent(buyerId, orderId, orderName)

        // when
        paymentCompleteService.markLedgerAsUpdated(orderId)
        paymentCompleteService.markWalletAsUpdated(orderId)

        // then - 지갑과 원장 모두 업데이트되었고 결제가 완료됨
        val payment = paymentEventRepository.findByOrderId(orderId)
        assertNotNull(payment!!)
        assertTrue(payment.isWalletUpdateDone())
        assertTrue(payment.isLedgerUpdateDone())
        assertTrue(payment.isPaymentDone)
    }

    fun prepareProduct(): List<Product> =
        productRepository.saveAll(
            listOf(
                Product(
                    id = 1,
                    amount = 1000,
                    quantity = 10,
                    name = "product1",
                    sellerId = 1,
                ),
                Product(
                    id = 2,
                    amount = 2000,
                    quantity = 20,
                    name = "product2",
                    sellerId = 2,
                ),
            ),
        )

    private fun prepareProductEvent(
        buyerId: Long,
        orderId: String,
        orderName: String,
    ) = paymentEventRepository.save(
        PaymentEvent
            .create(
                buyerId = buyerId,
                orderId = orderId,
                orderName = orderName,
            ).apply {
                addOrders(
                    listOf(
                        PaymentOrder.create(
                            sellerId = 1,
                            orderId = orderId,
                            amount = 1000,
                            productId = 1,
                            paymentOrderStatus = PaymentStatus.EXECUTING,
                            paymentEvent = this,
                        ),
                        PaymentOrder.create(
                            sellerId = 2,
                            orderId = orderId,
                            amount = 2000,
                            productId = 2,
                            paymentOrderStatus = PaymentStatus.EXECUTING,
                            paymentEvent = this,
                        ),
                    ),
                )

                updateStatus(
                    paymentKey = "paymentKey",
                    orderId = orderId,
                    status = PaymentStatus.SUCCESS,
                    extraDetails =
                        PaymentExtraDetails(
                            type = PaymentType.NORMAL,
                            method = PaymentMethod.EASY_PAY,
                            totalAmount = 3000,
                            orderName = "success_order_name",
                            approvedAt = LocalDateTime.now(),
                            pspConfirmationStatus = PSPConfirmationStatus.DONE,
                            pspRawData = "success_raw_data",
                        ),
                    failure = null,
                )
            },
    )
}
