@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

data class PaymentCheckoutCommand(
    val buyerId: Long,
    val productIds: List<Long>,
    val idempotencyKey: String,
)

data class PaymentCheckoutResult(
    val amount: Long,
    val orderId: String,
    val orderName: String,
)

@Service
@Transactional
class PaymentCheckoutService(
    private val productRepository: ProductRepository,
    private val paymentEventRepository: PaymentEventRepository,
) {
    fun checkout(command: PaymentCheckoutCommand): PaymentCheckoutResult {
        val existingPaymentEvent = paymentEventRepository.findByOrderId(command.idempotencyKey)
        require(existingPaymentEvent == null) { "Payment event with orderId ${command.idempotencyKey} already exists" }

        val products = productRepository.getProductsBy(command.productIds)
        check(products.size == command.productIds.size) {
            "Some products not found for productIds: ${command.productIds}"
        }

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
                                paymentEvent = this,
                            )
                        },
                    )

                    paymentEventRepository.save(this)
                }

        return PaymentCheckoutResult(
            amount = paymentEvent.totalAmount(),
            orderId = paymentEvent.orderId,
            orderName = paymentEvent.orderName,
        )
    }
}
