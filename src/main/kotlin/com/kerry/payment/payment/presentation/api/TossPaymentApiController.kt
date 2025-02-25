package com.kerry.payment.payment.presentation.api

import com.kerry.payment.payment.application.PaymentConfirmCommand
import com.kerry.payment.payment.application.PaymentConfirmResult
import com.kerry.payment.payment.application.PaymentConfirmService
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import com.kerry.payment.payment.presentation.api.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/toss")
class TossPaymentApiController(
    private val paymentConfirmService: PaymentConfirmService,
) {
    @PostMapping("/confirm")
    fun confirmPayment(
        @RequestBody req: TossPaymentConfirmRequest,
    ): ResponseEntity<ApiResponse<PaymentConfirmResult>> {
        val result =
            paymentConfirmService.confirm(
                PaymentConfirmCommand(
                    paymentKey = req.paymentKey,
                    orderId = req.orderId,
                    amount = req.amount,
                ),
            )

        return ResponseEntity.ok(ApiResponse.with(HttpStatus.OK, "Success", result))
    }
}
