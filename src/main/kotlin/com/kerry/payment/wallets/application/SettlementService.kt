package com.kerry.payment.wallets.application

import com.kerry.payment.payment.domain.PaymentOrder
import com.kerry.payment.wallets.domain.ReferenceType
import com.kerry.payment.wallets.domain.WalletRepository
import com.kerry.payment.wallets.domain.WalletTransactionRepository
import com.kerry.payment.wallets.domain.event.WalletEventMessage
import com.kerry.payment.wallets.domain.event.WalletEventMessageType
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
@Transactional
class SettlementService(
    private val walletRepository: WalletRepository,
    private val walletTransactionRepository: WalletTransactionRepository,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun processSettlement(
        paymentEventId: Long,
        orderId: String,
        paymentOrders: List<PaymentOrder>,
    ) {
        if (walletTransactionRepository.existsByOrderId(orderId)) {
            return
        }

        val paymentOrdersBySellerId = paymentOrders.groupBy { it.sellerId }
        val sellerIds = paymentOrdersBySellerId.keys

        val wallets = walletRepository.getWalletsBySellerIds(sellerIds)
        wallets.map {
            val paymentOrder = paymentOrdersBySellerId[it.userId]
            val totalAmount = paymentOrder!!.sumOf { order -> order.amount }
            it.deposit(
                orderId = orderId,
                amount = totalAmount,
                referenceType = ReferenceType.PAYMENT,
                referenceId = paymentEventId,
            )
        }

        walletRepository.saveAll(wallets)
        applicationEventPublisher.publishEvent(
            WalletEventMessage(
                orderId = orderId,
                type = WalletEventMessageType.SUCCESS,
            ),
        )
    }
}
