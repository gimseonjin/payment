package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.TossRestTemplate
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

data class PaymentConfirmCommand(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

data class PaymentConfirmResult(
    val status: PaymentStatus,
    val failure: PaymentFailure? = null,
) {
    init {
        if (status == PaymentStatus.FAILURE) {
            requireNotNull(failure) {
                "결제 상태 FAILURE 일 때 PaymentExecutionFailure 는 null 값이 될 수 없습니다."
            }
        }
    }

    val message =
        when (status) {
            PaymentStatus.SUCCESS -> "결제 처리에 성공하였습니다."
            PaymentStatus.FAILURE -> "결제 처리에 실패하였습니다."
            PaymentStatus.UNKNOWN -> "결제 처리 중에 알 수 없는 에러가 발생하였습니다."
            else -> error("현재 결제 상태 (status: $status) 는 올바르지 않은 상태입니다. ")
        }
}

@Service
class PaymentConfirmService(
    private val paymentEventRepository: PaymentEventRepository,
    private val tossRestTemplate: TossRestTemplate,
) {
    @Transactional
    fun confirm(confirmCommand: PaymentConfirmCommand): PaymentConfirmResult {
        val paymentEvent =
            paymentEventRepository.findByOrderId(confirmCommand.orderId)
                ?: throw IllegalArgumentException("Payment event not found")

        paymentEvent.updateStatusToExecuting()
        paymentEvent.isValid(confirmCommand.amount)

        val confirmResult: PaymentExecutionResult =
            tossRestTemplate.confirmPayment(
                TossPaymentConfirmRequest(
                    orderId = paymentEvent.orderId,
                    amount = paymentEvent.totalAmount(),
                    paymentKey = confirmCommand.paymentKey,
                ),
            )

        paymentEvent.updateStatus(
            paymentKey = confirmResult.paymentKey,
            orderId = confirmResult.orderId,
            status = confirmResult.paymentStatus(),
            extraDetails = confirmResult.extraDetails,
            failure = confirmResult.failure,
        )

        paymentEventRepository.save(paymentEvent)
        return PaymentConfirmResult(
            status = confirmResult.paymentStatus(),
            failure = confirmResult.failure,
        )
    }
}
