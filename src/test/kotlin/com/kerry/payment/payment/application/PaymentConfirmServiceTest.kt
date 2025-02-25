@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.TossRestTemplate
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.UUID

class PaymentConfirmServiceTest : BaseServiceTest() {
    @Autowired
    lateinit var checkoutService: PaymentCheckoutService

    @Autowired
    lateinit var productRepository: ProductRepository

    @Autowired
    lateinit var paymentEventRepository: PaymentEventRepository

    @Autowired
    lateinit var paymentConfirmService: PaymentConfirmService

    @MockkBean
    lateinit var tossRestTemplate: TossRestTemplate

    @Test
    fun `should be SUCCESS if payment confirm is success in PSP`() {
        prepareProduct()
        val orderId = UUID.randomUUID().toString()

        val confirmCommand =
            PaymentCheckoutCommand(
                idempotencyKey = orderId,
                buyerId = 1L,
                productIds = listOf(1L, 2L),
            )

        val checkoutResult = checkoutService.checkout(confirmCommand)

        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = UUID.randomUUID().toString(),
                orderId = checkoutResult.orderId,
                amount = checkoutResult.amount,
            )

        val successPaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentConfirmCommand.paymentKey,
                orderId = paymentConfirmCommand.orderId,
                extraDetails =
                    PaymentExtraDetails(
                        type = PaymentType.NORMAL,
                        method = PaymentMethod.EASY_PAY,
                        totalAmount = paymentConfirmCommand.amount,
                        orderName = "success_order_name",
                        approvedAt = LocalDateTime.now(),
                        pspConfirmationStatus = PSPConfirmationStatus.DONE,
                        pspRawData = "success_raw_data",
                    ),
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false,
            )

        every { tossRestTemplate.confirmPayment(any()) } returns successPaymentConfirmExecutionResult

        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(paymentConfirmCommand.orderId)

        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.SUCCESS)
        assertThat(savedPaymentEvent!!.orders.all { it.paymentOrderStatus == PaymentStatus.SUCCESS }).isTrue()
        assertThat(savedPaymentEvent.type).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.type)
        assertThat(savedPaymentEvent.method).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.method)
        assertThat(savedPaymentEvent.orderName).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.orderName)
        assertThat(savedPaymentEvent.approvedAt).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.approvedAt)
    }

    @Test
    fun `should be FAILURE if payment confirm is failure in PSP`() {
        prepareProduct()
        val orderId = UUID.randomUUID().toString()

        val confirmCommand =
            PaymentCheckoutCommand(
                idempotencyKey = orderId,
                buyerId = 1L,
                productIds = listOf(1L, 2L),
            )

        val checkoutResult = checkoutService.checkout(confirmCommand)

        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = UUID.randomUUID().toString(),
                orderId = checkoutResult.orderId,
                amount = checkoutResult.amount,
            )

        val failurePaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentConfirmCommand.paymentKey,
                orderId = paymentConfirmCommand.orderId,
                extraDetails = null,
                failure =
                    PaymentFailure(
                        errorCode = "ERROR_CODE",
                        message = "failure_message",
                    ),
                isSuccess = false,
                isFailure = true,
                isUnknown = false,
                isRetryable = false,
            )

        every { tossRestTemplate.confirmPayment(any()) } returns failurePaymentConfirmExecutionResult

        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(paymentConfirmCommand.orderId)

        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(savedPaymentEvent!!.orders.all { it.paymentOrderStatus == PaymentStatus.FAILURE }).isTrue()
    }

    fun prepareProduct() {
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
    }
}
