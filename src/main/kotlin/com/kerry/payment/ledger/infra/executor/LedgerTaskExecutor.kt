package com.kerry.payment.ledger.infra.executor

import com.kerry.payment.ledger.application.DoubleLedgerEntryRecordService
import com.kerry.payment.ledger.application.LedgerVerificationService
import com.kerry.payment.payment.domain.event.PaymentConfirmedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener
import java.time.LocalDateTime

@Component
class LedgerTaskExecutor(
    private val doubleLedgerEntryRecordService: DoubleLedgerEntryRecordService,
    private val ledgerVerificationService: LedgerVerificationService,
) {
    private val logger: Logger = LoggerFactory.getLogger(LedgerTaskExecutor::class.java)

    @TransactionalEventListener
    fun execute(event: PaymentConfirmedEvent) {
        doubleLedgerEntryRecordService.recordDoubleLedgerEntry(
            paymentEventId = event.paymentEventId,
            orderId = event.orderId,
            paymentOrders = event.paymentOrders,
        )
    }

    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정 (00:00:00) 실행
    fun runLedgerVerificationTask() {
        val now = LocalDateTime.now()
        val from =
            now
                .minusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0) // 전날 00:00:00
        val to =
            now
                .minusDays(1)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0) // 전날 23:59:59

        logger.info("🔄 Running ledger verification task for {} to {}", from, to)

        ledgerVerificationService.verifyLedger(from, to)
    }
}
