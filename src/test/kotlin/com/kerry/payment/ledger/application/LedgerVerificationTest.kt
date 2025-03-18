package com.kerry.payment.ledger.application

import com.kerry.payment.BaseTest
import com.kerry.payment.common.IdempotencyCreator
import com.kerry.payment.ledger.domain.*
import com.kerry.payment.wallets.domain.ReferenceType
import io.mockk.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import java.time.LocalDateTime
import kotlin.test.Test

class LedgerVerificationTest : BaseTest() {
    @Autowired
    lateinit var ledgerRepository: LedgerRepository

    @Autowired
    lateinit var ledgerVerificationService: LedgerVerificationService

    @Test
    fun `should detect ledger discrepancies correctly and notify`() {
        // Given - 테스트용 Ledger 데이터 삽입 (불일치 발생)
        val from =
            LocalDateTime
                .now()
                .minusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        val to =
            LocalDateTime
                .now()
                .minusDays(1)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0)

        prepareLedgerDataWithDiscrepancy(from) // 불일치 데이터 삽입

        every { ledgerVerificationNotifier.notifyDiscrepancy(any()) } just Runs

        // When - Ledger 검증 실행
        val discrepancies: List<LedgerDiscrepancy> = ledgerVerificationService.verifyLedger(from, to)

        // Then - 불일치가 감지되어야 함
        assertEquals(1, discrepancies.size)
        assertEquals(from.toLocalDate(), discrepancies[0].transactionDate)
        assertEquals(10000, discrepancies[0].totalCredit - discrepancies[0].totalDebit) // 차액 검증

        // Notification이 호출되었는지 검증
        val slot = slot<String>()
        verify(exactly = 1) { ledgerVerificationNotifier.notifyDiscrepancy(capture(slot)) }

        // Notification 메시지 검증
        val notificationMessage = slot.captured
        assert(notificationMessage.contains("🚨 Ledger Discrepancy Detected!"))
        assert(notificationMessage.contains("Credit 50000 ≠ Debit 40000 (Diff: 10000)"))
    }

    @Test
    fun `should not detect discrepancies when ledger is balanced and not notify`() {
        // Given - 균형이 맞는 Ledger 데이터 삽입
        val from =
            LocalDateTime
                .now()
                .minusDays(1)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
        val to =
            LocalDateTime
                .now()
                .minusDays(1)
                .withHour(23)
                .withMinute(59)
                .withSecond(59)
                .withNano(0)

        prepareBalancedLedgerData(from) // 불일치 없는 데이터 삽입

        // When - Ledger 검증 실행
        val discrepancies: List<LedgerDiscrepancy> = ledgerVerificationService.verifyLedger(from, to)

        // Then - 불일치가 없어야 함
        assertEquals(0, discrepancies.size)

        // Notification이 호출되지 않았는지 검증
        verify(exactly = 0) { ledgerVerificationNotifier.notifyDiscrepancy(any()) }
    }

    private fun prepareLedgerDataWithDiscrepancy(date: LocalDateTime) {
        val transaction =
            LedgerTransaction(
                id = null,
                referenceType = ReferenceType.PAYMENT,
                referenceId = 1L,
                orderId = "orderId",
                description = "LedgerService record transaction",
                idempotencyKey = null,
                createdAt = LocalDateTime.now(),
            ).apply {
                idempotencyKey = IdempotencyCreator.create(this)
            }

        ledgerRepository.saveAll(
            listOf(
                Ledger(
                    id = null,
                    accountId = 1L,
                    amount = 50000,
                    type = LedgerType.CREDIT,
                    createdAt = date,
                    transaction = transaction,
                ),
                Ledger(
                    id = null,
                    accountId = 2L,
                    amount = 40000,
                    type = LedgerType.DEBIT,
                    createdAt = date,
                    transaction = transaction,
                ),
            ),
        )
    }

    private fun prepareBalancedLedgerData(date: LocalDateTime) {
        val transaction =
            LedgerTransaction(
                id = null,
                referenceType = ReferenceType.PAYMENT,
                referenceId = 1L,
                orderId = "orderId",
                description = "LedgerService record transaction",
                idempotencyKey = null,
                createdAt = LocalDateTime.now(),
            ).apply {
                idempotencyKey = IdempotencyCreator.create(this)
            }

        ledgerRepository.saveAll(
            listOf(
                Ledger(
                    id = null,
                    accountId = 1L,
                    amount = 50000,
                    type = LedgerType.CREDIT,
                    createdAt = date,
                    transaction = transaction,
                ),
                Ledger(
                    id = null,
                    accountId = 2L,
                    amount = 50000,
                    type = LedgerType.DEBIT,
                    createdAt = date,
                    transaction = transaction,
                ),
            ),
        )
    }
}
