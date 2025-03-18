package com.kerry.payment.payment.infra.executor

import com.kerry.payment.payment.application.PaymentConfirmService
import jakarta.transaction.Transactional
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
@Transactional
class PaymentRecoveryTaskExecutor(
    private val paymentConfirmService: PaymentConfirmService,
) {
    @Scheduled(fixedDelay = 100, timeUnit = TimeUnit.SECONDS)
    fun execute() {
        val from = LocalDateTime.now().minusMinutes(3)
        val to = LocalDateTime.now()
        val limit = 10

        paymentConfirmService.recover(from, to, limit)
    }
}
