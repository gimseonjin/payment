package com.kerry.payment.wallets.application

import com.kerry.payment.payment.application.BaseServiceTest
import com.kerry.payment.payment.domain.PaymentEvent
import com.kerry.payment.payment.domain.PaymentOrder
import com.kerry.payment.payment.domain.PaymentStatus
import com.kerry.payment.wallets.domain.Wallet
import com.kerry.payment.wallets.domain.WalletRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class SettlementServiceTest : BaseServiceTest() {
    @Autowired
    lateinit var settlementService: SettlementService

    @Autowired
    lateinit var walletRepository: WalletRepository

    @Test
    fun `should process settlement successfully`() {
        // given
        val wallets = prepareWallets()
        val paymentEventId = 1L
        val orderId = "order-1"
        val paymentOrders = preparePaymentOrders(orderId)

        // when
        settlementService.processSettlement(paymentEventId, orderId, paymentOrders)

        // then
        val updatedWallets =
            walletRepository.getWalletsBySellerIds(
                wallets.map { it.userId }.toSet(),
            )

        // then
        assertThat(updatedWallets).hasSize(2)

        assertThat(updatedWallets[0].userId).isEqualTo(1)
        assertThat(updatedWallets[0].balance).isEqualTo(3000)
        assertThat(updatedWallets[0].transactions).hasSize(1)

        assertThat(updatedWallets[1].userId).isEqualTo(2)
        assertThat(updatedWallets[1].balance).isEqualTo(4000)
        assertThat(updatedWallets[1].transactions).hasSize(1)
    }

    @Test
    fun `success only once even if called multiple times`() {
        // given
        val wallets = prepareWallets()
        val paymentEventId = 1L
        val orderId = "order-1"
        val paymentOrders = preparePaymentOrders(orderId)

        // when
        settlementService.processSettlement(paymentEventId, orderId, paymentOrders)
        settlementService.processSettlement(paymentEventId, orderId, paymentOrders)
        settlementService.processSettlement(paymentEventId, orderId, paymentOrders)

        // then
        val updatedWallets =
            walletRepository.getWalletsBySellerIds(
                wallets.map { it.userId }.toSet(),
            )

        // then
        assertThat(updatedWallets).hasSize(2)

        assertThat(updatedWallets[0].userId).isEqualTo(1)
        assertThat(updatedWallets[0].balance).isEqualTo(3000)
        assertThat(updatedWallets[0].transactions).hasSize(1)

        assertThat(updatedWallets[1].userId).isEqualTo(2)
        assertThat(updatedWallets[1].balance).isEqualTo(4000)
        assertThat(updatedWallets[1].transactions).hasSize(1)
    }

    private fun prepareWallets(): List<Wallet> {
        val wallets =
            listOf(
                Wallet.create(
                    userId = 1,
                    balance = 0,
                ),
                Wallet.create(
                    userId = 2,
                    balance = 0,
                ),
            )
        return walletRepository.saveAll(wallets)
    }

    private fun preparePaymentOrders(orderId: String): List<PaymentOrder> =
        listOf(
            PaymentOrder.create(
                sellerId = 1,
                productId = 1,
                orderId = orderId,
                amount = 3000,
                paymentOrderStatus = PaymentStatus.SUCCESS,
                paymentEvent =
                    PaymentEvent.create(
                        buyerId = 1,
                        orderId = orderId,
                        orderName = "order-1",
                    ),
            ),
            PaymentOrder.create(
                sellerId = 2,
                productId = 2,
                orderId = orderId,
                amount = 4000,
                paymentOrderStatus = PaymentStatus.SUCCESS,
                paymentEvent =
                    PaymentEvent.create(
                        buyerId = 1,
                        orderId = orderId,
                        orderName = "order-2",
                    ),
            ),
        )
}
