package com.kerry.payment.payment.application

import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.domain.event.PaymentConfirmedEvent
import com.kerry.payment.payment.infra.TossRestTemplate
import com.kerry.payment.payment.infra.response.PSPConfirmationException
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import jakarta.transaction.Transactional
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import java.util.concurrent.TimeoutException

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
@Transactional
class PaymentConfirmService(
    private val paymentEventRepository: PaymentEventRepository,
    private val tossRestTemplate: TossRestTemplate,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    fun confirm(confirmCommand: PaymentConfirmCommand): PaymentConfirmResult {
        val paymentEvent =
            paymentEventRepository.findByOrderId(confirmCommand.orderId)
                ?: throw IllegalArgumentException("Payment event not found")

        paymentEvent.updateStatusToExecuting()

        return runCatching {
            paymentEvent.isValid(confirmCommand.amount)

            val confirmResult =
                tossRestTemplate.confirmPayment(
                    TossPaymentConfirmRequest(
                        orderId = paymentEvent.orderId,
                        amount = paymentEvent.totalAmount(),
                        paymentKey = confirmCommand.paymentKey,
                    ),
                )

            paymentEvent.updateStatus(
                paymentKey = confirmCommand.paymentKey,
                orderId = confirmCommand.orderId,
                status = confirmResult.paymentStatus(),
                extraDetails = confirmResult.extraDetails,
                failure = null,
            )

            confirmResult
        }.fold(
            onSuccess = { confirmResult ->
                paymentEventRepository.save(paymentEvent)

                applicationEventPublisher.publishEvent(
                    PaymentConfirmedEvent(
                        paymentEventId = paymentEvent.id!!,
                        orderId = paymentEvent.orderId,
                        paymentOrders = paymentEvent.orders,
                    ),
                )

                PaymentConfirmResult(status = confirmResult.paymentStatus(), failure = null)
            },
            onFailure = { ex ->
                val (status, failure) = mapExceptionToFailure(ex)

                paymentEvent.updateStatus(
                    paymentKey = confirmCommand.paymentKey,
                    orderId = confirmCommand.orderId,
                    status = status,
                    extraDetails = null,
                    failure = failure,
                )

                paymentEventRepository.save(paymentEvent)
                PaymentConfirmResult(status = status, failure = failure)
            },
        )
    }

    private fun mapExceptionToFailure(ex: Throwable): Pair<PaymentStatus, PaymentFailure> =
        when (ex) {
            is PSPConfirmationException ->
                ex.paymentStatus() to
                    PaymentFailure(
                        errorCode = ex.errorCode,
                        message = ex.errorMessage,
                    )

            is PaymentValidationException ->
                PaymentStatus.FAILURE to
                    PaymentFailure(
                        errorCode = ex::class.simpleName ?: "PaymentValidationException",
                        message = ex.message ?: "결제 검증 중 오류가 발생했습니다.",
                    )

            is PaymentAlreadyProcessedException ->
                ex.status to
                    PaymentFailure(
                        errorCode = ex::class.simpleName ?: "PaymentAlreadyProcessedException",
                        message = ex.message ?: "이미 처리된 결제입니다.",
                    )

            is TimeoutException ->
                PaymentStatus.UNKNOWN to
                    PaymentFailure(
                        errorCode = ex::class.simpleName ?: "TimeoutException",
                        message = ex.message ?: "결제 요청이 시간 초과되었습니다.",
                    )

            else ->
                PaymentStatus.UNKNOWN to
                    PaymentFailure(
                        errorCode = ex::class.simpleName ?: "UnknownException",
                        message = ex.message ?: "알 수 없는 에러가 발생하였습니다.",
                    )
        }
}
