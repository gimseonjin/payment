package com.kerry.payment.payment.presentation.executor

import com.kerry.payment.payment.application.PaymentCheckoutCommand
import com.kerry.payment.payment.application.PaymentCheckoutService
import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.TossRestTemplate
import com.kerry.payment.payment.infra.response.PSPConfirmationException
import com.kerry.payment.payment.presentation.BasePresentationTest
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import java.util.*

class PaymentRecoveryTaskExecutorTest : BasePresentationTest() {
    @Autowired
    private lateinit var paymentRecoveryTaskExecutor: PaymentRecoveryTaskExecutor

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var paymentCheckoutService: PaymentCheckoutService

    @Autowired
    private lateinit var paymentEventRepository: PaymentEventRepository

    @MockkBean
    lateinit var tossRestTemplate: TossRestTemplate

    @Test
    fun `should recover payment`() {
        // given
        val products = prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command =
            PaymentCheckoutCommand(
                buyerId = 1,
                productIds = products.map { it.id },
                idempotencyKey = orderId,
            )
        val checkoutResult = paymentCheckoutService.checkout(command)

        val paymentEvent = paymentEventRepository.findByOrderId(checkoutResult.orderId)!!
        val paymentKey = UUID.randomUUID().toString()
        paymentEvent.updateStatus(
            paymentKey = paymentKey,
            orderId = checkoutResult.orderId,
            status = PaymentStatus.UNKNOWN,
            extraDetails = null,
            failure =
                PaymentFailure(
                    errorCode = "code",
                    message = "message",
                ),
        )
        paymentEventRepository.save(paymentEvent)

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
        paymentRecoveryTaskExecutor.execute()

        // then
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)!!
        assertTrue(savedPaymentEvent.isSuccess())
    }

    @Test
    fun `should fail to recovery when an unknown exception occurs`() {
        // given
        val products = prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command =
            PaymentCheckoutCommand(
                buyerId = 1,
                productIds = products.map { it.id },
                idempotencyKey = orderId,
            )
        val checkoutResult = paymentCheckoutService.checkout(command)

        val paymentEvent = paymentEventRepository.findByOrderId(checkoutResult.orderId)!!
        val paymentKey = UUID.randomUUID().toString()
        paymentEvent.updateStatus(
            paymentKey = paymentKey,
            orderId = checkoutResult.orderId,
            status = PaymentStatus.UNKNOWN,
            extraDetails = null,
            failure =
                PaymentFailure(
                    errorCode = "code",
                    message = "message",
                ),
        )
        paymentEventRepository.save(paymentEvent)

        every { tossRestTemplate.confirmPayment(any()) } throws
            PSPConfirmationException(
                errorCode = "UNKNOWN",
                errorMessage = "Unknown error",
                isSuccess = false,
                isFailure = false,
                isUnknown = true,
                isRetryableError = false,
                cause = null,
            )

        // when
        paymentRecoveryTaskExecutor.execute()

        // then
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)!!
        assertTrue(savedPaymentEvent.isUnknown())
    }

    @Test
    fun `should fail to recovery when an failuer exception occurs`() {
        // given
        val products = prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command =
            PaymentCheckoutCommand(
                buyerId = 1,
                productIds = products.map { it.id },
                idempotencyKey = orderId,
            )
        val checkoutResult = paymentCheckoutService.checkout(command)

        val paymentEvent = paymentEventRepository.findByOrderId(checkoutResult.orderId)!!
        val paymentKey = UUID.randomUUID().toString()
        paymentEvent.updateStatus(
            paymentKey = paymentKey,
            orderId = checkoutResult.orderId,
            status = PaymentStatus.UNKNOWN,
            extraDetails = null,
            failure =
                PaymentFailure(
                    errorCode = "code",
                    message = "message",
                ),
        )
        paymentEventRepository.save(paymentEvent)

        every { tossRestTemplate.confirmPayment(any()) } throws
            PSPConfirmationException(
                errorCode = "FAILURE",
                errorMessage = "Failure error",
                isSuccess = false,
                isFailure = true,
                isUnknown = false,
                isRetryableError = false,
                cause = null,
            )

        // when
        paymentRecoveryTaskExecutor.execute()

        // then
        val savedPaymentEvent = paymentEventRepository.findByOrderId(orderId)!!
        assertTrue(savedPaymentEvent.isFailure())
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
}
