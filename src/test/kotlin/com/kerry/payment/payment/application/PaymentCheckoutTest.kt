package com.kerry.payment.payment.application

import com.kerry.payment.BaseTest
import com.kerry.payment.payment.domain.PaymentEventRepository
import com.kerry.payment.payment.domain.Product
import com.kerry.payment.payment.domain.ProductRepository
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import java.util.UUID
import kotlin.test.Test

class PaymentCheckoutTest : BaseTest() {
    @Autowired
    private lateinit var paymentCheckoutService: PaymentCheckoutService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var paymentEventRepository: PaymentEventRepository

    @Test
    fun `should save payment event and orders successfully`() {
        // Given
        prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command =
            PaymentCheckoutCommand(
                buyerId = 1,
                productIds = listOf(1, 2),
                idempotencyKey = orderId,
            )

        // When
        val result = paymentCheckoutService.checkout(command)

        // Then
        assertNotNull(result)
        assertEquals(3000, result.amount)
        assertEquals(orderId, result.orderId)
        assertEquals("product1, product2", result.orderName)

        paymentEventRepository.findByOrderId("idempotencyKey")?.let { paymentEvent ->
            assertEquals(3000, paymentEvent.totalAmount())
            assertEquals("idempotencyKey", paymentEvent.orderId)
            assertEquals("product1, product2", paymentEvent.orderName)
            assertEquals(2, paymentEvent.orders.size)

            paymentEvent.orders.forEach { order ->
                assertEquals(orderId, order.orderId)
                assertEquals("NOT_STARTED", order.paymentOrderStatus.name)
                assertEquals(false, order.isLedgerUpdated())
                assertEquals(false, order.isWalletUpdated())
            }
        }
    }

    @Test
    fun `should throw exception when trying to save in second time with same idempotency key`() {
        // Given
        prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command =
            PaymentCheckoutCommand(
                buyerId = 1,
                productIds = listOf(1, 2),
                idempotencyKey = orderId,
            )

        // When
        paymentCheckoutService.checkout(command)

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            paymentCheckoutService.checkout(command)
        }
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
