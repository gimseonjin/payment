package com.kerry.payment.ledger.domain

import org.springframework.data.jpa.repository.JpaRepository

interface LedgerRepository : JpaRepository<Ledger, Long> 
