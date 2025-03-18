package com.kerry.payment.ledger.domain.event

data class LedgerEventMessage(
    val type: LedgerEventMessageType,
    val orderId: String,
)

enum class LedgerEventMessageType(
    description: String,
) {
    SUCCESS("장부 기입 성공"),
}
