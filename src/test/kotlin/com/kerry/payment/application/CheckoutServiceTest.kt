package com.kerry.payment.application

import com.kerry.payment.domain.PaymentEventRepository
import com.kerry.payment.domain.Product
import com.kerry.payment.domain.ProductRepository
import jakarta.transaction.Transactional
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
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
    fun `checkout should return CheckoutResult`() {
        // Given
        prepareProduct()
        val command = CheckoutCommand(
            buyerId = 1,
            productIds = listOf(1, 2),
            idempotencyKey = "idempotencyKey"
        )

        // When
        val result = checkoutService.checkout(command)

        // Then
        assertNotNull(result)
        assertEquals(3000, result.amount)
        assertEquals("idempotencyKey", result.orderId)
        assertEquals("product1, product2", result.orderName)

        paymentEventRepository.findByOrderId("idempotencyKey")?.let { paymentEvent ->
            assertEquals(3000, paymentEvent.totalAmount())
            assertEquals("idempotencyKey", paymentEvent.orderId)
            assertEquals("product1, product2", paymentEvent.orderName)
            assertEquals(2, paymentEvent.orders.size)
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