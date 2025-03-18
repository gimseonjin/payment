package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.PaymentEventRepository
import com.kerry.payment.payment.domain.PaymentOrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentCompleteService(
    private val paymentOrderRepository: PaymentOrderRepository,
    private val paymentEventRepository: PaymentEventRepository,
) {
    fun markLedgerAsUpdated(orderId: String) {
        paymentOrderRepository.updateLedgerStatus(orderId)
        paymentEventRepository.updatePaymentDoneIfAllOrdersUpdated(orderId)
    }

    fun markWalletAsUpdated(orderId: String) {
        paymentOrderRepository.updateWalletStatus(orderId)
        paymentEventRepository.updatePaymentDoneIfAllOrdersUpdated(orderId)
    }
}
