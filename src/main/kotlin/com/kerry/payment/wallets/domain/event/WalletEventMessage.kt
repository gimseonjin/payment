package com.kerry.payment.wallets.domain.event

data class WalletEventMessage(
    val type: WalletEventMessageType,
    val orderId: String,
)

enum class WalletEventMessageType(
    description: String,
) {
    SUCCESS("정산 성공"),
}
