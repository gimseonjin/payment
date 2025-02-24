package com.kerry.payment.application

import com.kerry.payment.payment.domain.PaymentEventRepository
import com.kerry.payment.payment.domain.Product
import com.kerry.payment.payment.domain.ProductRepository
import com.kerry.payment.payment.application.CheckoutCommand
import com.kerry.payment.payment.application.CheckoutService
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.Test

@SpringBootTest
@Transactional
class CheckoutServiceTest {

    @Autowired
    private lateinit var checkoutService: CheckoutService

    @Autowired
    private lateinit var productRepository: ProductRepository

    @Autowired
    private lateinit var paymentEventRepository: PaymentEventRepository

    @Test
    fun `should save payment event and orders successfully`() {
        // Given
        prepareProduct()

        val orderId = UUID.randomUUID().toString()
        val command = CheckoutCommand(
            buyerId = 1,
            productIds = listOf(1, 2),
            idempotencyKey = orderId
        )

        // When
        val result = checkoutService.checkout(command)

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
        val command = CheckoutCommand(
            buyerId = 1,
            productIds = listOf(1, 2),
            idempotencyKey = orderId
        )

        // When
        checkoutService.checkout(command)

        // Then
        assertThrows(IllegalArgumentException::class.java) {
            checkoutService.checkout(command)
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
                    sellerId = 1
                ),
                Product(
                    id = 2,
                    amount = 2000,
                    quantity = 20,
                    name = "product2",
                    sellerId = 2
                )
            )
        )
    }
}