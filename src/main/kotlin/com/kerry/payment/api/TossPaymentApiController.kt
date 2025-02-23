package com.kerry.payment.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate

data class TossPaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: Long,
)

data class ApiResponse<T>(
    val status: Int = 200,
    val massage : String,
    val data: T? = null
) {
    companion object {
        fun <T> with(httpStatus: HttpStatus, message: String, data: T? = null): ApiResponse<T> {
            return ApiResponse(
                status = httpStatus.value(),
                massage = message,
                data = data
            )
        }
    }
}

@RestController
@RequestMapping("/v1/toss")
class TossPaymentApiController(
    private val tossRestTemplate: RestTemplate
) {

    @PostMapping("/confirm")
    fun confirmPayment(
        @RequestBody req: TossPaymentConfirmRequest
    ): ResponseEntity<ApiResponse<String>> {
        val result: ResponseEntity<String> = tossRestTemplate.postForEntity(
            "/v1/payments/confirm",
            req,
            String::class.java
        )
        return ResponseEntity.ok(ApiResponse.with(HttpStatus.OK, "Success", result.body))
    }
}