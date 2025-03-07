package com.kerry.payment.wallets.domain

import jakarta.persistence.*

@Entity
data class Wallet(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long,
    private var userId: Long,
    private var balance: Long,
    private var createdAt: Long,
    private var updatedAt: Long,
    @Version
    private var version: Long = 0,
)
