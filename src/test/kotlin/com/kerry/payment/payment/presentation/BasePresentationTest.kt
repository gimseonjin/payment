package com.kerry.payment.payment.presentation

import jakarta.transaction.Transactional
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BasePresentationTest
