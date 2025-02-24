@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

data class CheckoutCommand(
    val buyerId: Long,
    val productIds: List<Long>,
    val idempotencyKey: String,
)

data class CheckoutResult(
    val amount: Long,
    val orderId: String,
    val orderName: String,
)

@Service
@Transactional
class CheckoutService(
    private val productRepository: ProductRepository,
    private val paymentEventRepository: PaymentEventRepository,
) {
    fun checkout(command: CheckoutCommand): CheckoutResult {
        val existingPaymentEvent = paymentEventRepository.findByOrderId(command.idempotencyKey)
        require(existingPaymentEvent == null) { "Payment event with orderId ${command.idempotencyKey} already exists" }

        val products = productRepository.getProductsBy(command.productIds)

        val paymentEvent =
            PaymentEvent
                .create(
                    buyerId = command.buyerId,
                    orderId = command.idempotencyKey,
                    orderName = products.joinToString { it.name },
                ).apply {
                    addOrders(
                        products.map { product ->
                            PaymentOrder.create(
                                sellerId = product.sellerId,
                                orderId = command.idempotencyKey,
                                amount = product.amount,
                                productId = product.id,
                                paymentOrderStatus = PaymentStatus.NOT_STARTED,
                            )
                        },
                    )

                    paymentEventRepository.save(this)
                }

        return CheckoutResult(
            amount = paymentEvent.totalAmount(),
            orderId = paymentEvent.orderId,
            orderName = paymentEvent.orderName,
        )
    }
}
