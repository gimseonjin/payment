package com.kerry.payment.payment.domain

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    @Query("select p from Product p where p.id in :productIds")
    fun getProductsBy(@Param("productIds") productIds: List<Long>): List<Product>
}