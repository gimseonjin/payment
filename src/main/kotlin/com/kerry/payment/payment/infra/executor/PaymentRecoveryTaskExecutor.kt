package com.kerry.payment.payment.infra.executor

import com.kerry.payment.payment.application.PaymentConfirmCommand
import com.kerry.payment.payment.application.PaymentConfirmService
import com.kerry.payment.payment.domain.PaymentEventRepository
import jakarta.transaction.Transactional
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
@Transactional
class PaymentRecoveryTaskExecutor(
    private val paymentEventRepository: PaymentEventRepository,
    private val paymentConfirmService: PaymentConfirmService,
) {
    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.SECONDS)
    fun execute() {
        val from = LocalDateTime.now().minusMinutes(3)
        val to = LocalDateTime.now()
        val limit = 10

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
            }.chunked(2)
            .forEach { chunk ->
                runBlocking {
                    chunk.map { async { paymentConfirmService.confirm(it) } }.awaitAll()
                }
            }
    }
}
