package com.kerry.payment.payment.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id

@Entity
data class Product(
    @Id
    val id: Long,
    @Column(nullable = false)
    val amount: Long,
    @Column(nullable = false)
    val quantity: Int,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val sellerId: Long,
)
