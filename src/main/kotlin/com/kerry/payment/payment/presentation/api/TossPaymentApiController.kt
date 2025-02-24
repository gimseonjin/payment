package com.kerry.payment.payment.presentation.api

import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import com.kerry.payment.payment.presentation.api.response.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

@RestController
@RequestMapping("/v1/toss")
class TossPaymentApiController(
    private val tossRestTemplate: RestTemplate,
) {
    @PostMapping("/confirm")
    fun confirmPayment(
        @RequestBody req: TossPaymentConfirmRequest,
    ): ResponseEntity<ApiResponse<String>> {
        val result: ResponseEntity<String> =
            tossRestTemplate.postForEntity(
                "/v1/payments/confirm",
                req,
                String::class.java,
            )

        return ResponseEntity.ok(ApiResponse.with(HttpStatus.OK, "Success", result.body))
    }
}
