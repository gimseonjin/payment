@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.TossRestTemplate
import com.kerry.payment.payment.infra.response.PSPConfirmationException
import com.kerry.payment.payment.infra.response.TossPaymentError
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
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
        // given
        val productIds = prepareProduct()
        val (checkoutResult, orderId) = performCheckout(productIds)

        val paymentKey = UUID.randomUUID().toString()
        val successPaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentKey,
                orderId = orderId,
                extraDetails =
                    PaymentExtraDetails(
                        type = PaymentType.NORMAL,
                        method = PaymentMethod.EASY_PAY,
                        totalAmount = checkoutResult.amount,
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

        // when
        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = paymentKey,
                orderId = orderId,
                amount = checkoutResult.amount,
            )
        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)

        // then
        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.SUCCESS)
        assertThat(savedPaymentEvent!!.orders.all { it.paymentOrderStatus == PaymentStatus.SUCCESS }).isTrue()
        assertThat(savedPaymentEvent.type).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.type)
        assertThat(savedPaymentEvent.method).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.method)
        assertThat(savedPaymentEvent.orderName).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.orderName)
        assertThat(savedPaymentEvent.approvedAt).isEqualTo(successPaymentConfirmExecutionResult.extraDetails!!.approvedAt)
    }

    @Test
    fun `should be FAILURE if payment confirm is failure in PSP`() {
        // given
        val productIds = prepareProduct()
        val (checkoutResult, orderId) = performCheckout(productIds)

        val paymentKey = UUID.randomUUID().toString()
        val failurePaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentKey,
                orderId = orderId,
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

        // when
        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = paymentKey,
                orderId = orderId,
                amount = checkoutResult.amount,
            )
        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)

        // then
        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(savedPaymentEvent!!.orders.all { it.paymentOrderStatus == PaymentStatus.FAILURE }).isTrue()
    }

    @Test
    fun `should be UNKNOWN if payment confirm is failure due to unknown exception in PSP`() {
        // given
        val productIds = prepareProduct()
        val (checkoutResult, orderId) = performCheckout(productIds)

        val paymentKey = UUID.randomUUID().toString()
        val unknownPaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentKey,
                orderId = orderId,
                extraDetails = null,
                failure = null,
                isSuccess = false,
                isFailure = false,
                isUnknown = true,
                isRetryable = false,
            )
        every { tossRestTemplate.confirmPayment(any()) } returns unknownPaymentConfirmExecutionResult

        // when
        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = paymentKey,
                orderId = orderId,
                amount = checkoutResult.amount,
            )
        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)

        // then
        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.UNKNOWN)
        assertThat(savedPaymentEvent!!.orders.all { it.paymentOrderStatus == PaymentStatus.UNKNOWN }).isTrue()
    }

    @Test
    fun `should handle PSPConfirmationException`() {
        // given
        val productIds = prepareProduct()
        val (checkoutResult, orderId) = performCheckout(productIds)

        val paymentKey = UUID.randomUUID().toString()
        val pspConfirmationException =
            PSPConfirmationException(
                errorCode = TossPaymentError.REJECT_ACCOUNT_PAYMENT.name,
                errorMessage = TossPaymentError.REJECT_ACCOUNT_PAYMENT.description,
                isSuccess = false,
                isFailure = true,
                isUnknown = false,
                isRetryableError = false,
            )
        every { tossRestTemplate.confirmPayment(any()) } throws pspConfirmationException

        // when
        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = paymentKey,
                orderId = orderId,
                amount = checkoutResult.amount,
            )
        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)

        // then
        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(savedPaymentEvent!!.isFailure()).isTrue()
    }

    @Test
    fun `should handle PaymentValidationException`() {
        // given
        val productIds = prepareProduct()
        val (checkoutResult, orderId) = performCheckout(productIds)

        val paymentKey = UUID.randomUUID().toString()
        val inValidationPaymentConfirmExecutionResult =
            PaymentExecutionResult(
                paymentKey = paymentKey,
                orderId = orderId,
                extraDetails = null,
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false,
            )
        every { tossRestTemplate.confirmPayment(any()) } returns inValidationPaymentConfirmExecutionResult

        // when
        val paymentConfirmCommand =
            PaymentConfirmCommand(
                paymentKey = paymentKey,
                orderId = orderId,
                amount = checkoutResult.amount,
            )
        val paymentConfirmResult = paymentConfirmService.confirm(paymentConfirmCommand)
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)

        // then
        assertThat(paymentConfirmResult.status).isEqualTo(PaymentStatus.FAILURE)
        assertThat(savedPaymentEvent!!.isFailure()).isTrue()
    }

    // checkout 준비 로직을 별도 함수로 분리, productIds를 매개변수로 받음
    private fun performCheckout(productIds: List<Long>): Pair<PaymentCheckoutResult, String> {
        val orderId = UUID.randomUUID().toString()
        val confirmCommand =
            PaymentCheckoutCommand(
                idempotencyKey = orderId,
                buyerId = 1L,
                productIds = productIds,
            )
        val checkoutResult = checkoutService.checkout(confirmCommand)
        return Pair(checkoutResult, orderId)
    }

    // 상품 준비 후 상품 ID 목록 반환
    fun prepareProduct(): List<Long> {
        val products =
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
            )
        productRepository.saveAll(products)
        return products.map { it.id }
    }
}
