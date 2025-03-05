package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.PaymentEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PaymentRecoveryService(
    private val paymentEventRepository: PaymentEventRepository,
) {
    suspend fun recover(
        from: LocalDateTime,
        to: LocalDateTime,
        limit: Int,
    ) {
        withContext(Dispatchers.IO) {
            paymentEventRepository
                .findRetryablePayments(
                    from = from,
                    to = to,
                    pageable = Pageable.ofSize(limit),
                ).map {
                    PaymentConfirmCommand(
                        paymentKey = it.paymentKey!!,
                        orderId = it.orderId,
                        amount = it.totalAmount(),
                    )
                }
        }
    }
}
