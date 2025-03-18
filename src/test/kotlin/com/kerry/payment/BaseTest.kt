package com.kerry.payment

import com.kerry.payment.ledger.domain.LedgerVerificationNotifier
import com.kerry.payment.payment.infra.TossRestTemplate
import com.ninjasquad.springmockk.MockkBean
import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Transactional
@ActiveProfiles("test")
abstract class BaseTest {
    @MockkBean
    lateinit var tossRestTemplate: TossRestTemplate

    @MockkBean
    lateinit var ledgerVerificationNotifier: LedgerVerificationNotifier
}
