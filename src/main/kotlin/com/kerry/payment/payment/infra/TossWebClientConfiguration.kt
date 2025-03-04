@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.infra

import com.fasterxml.jackson.databind.ObjectMapper
import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.response.PSPConfirmationException
import com.kerry.payment.payment.infra.response.TossFailureResponse
import com.kerry.payment.payment.infra.response.TossPaymentConfirmationResponse
import com.kerry.payment.payment.infra.response.TossPaymentError
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Configuration
class TossWebClientConfiguration(
    @Value("\${PSP.toss.url}") private val baseUrl: String,
    @Value("\${PSP.toss.secret-key}") private val secretKey: String,
) {
    @Bean
    fun tossRestTemplate(): TossRestTemplate =
        TossRestTemplateImpl(
            baseUrl,
            secretKey,
            RestTemplate().apply {
                uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
                interceptors.add(
                    ClientHttpRequestInterceptor { request, body, execution ->
                        val encodedSecretKey = Base64.getEncoder().encodeToString(("$secretKey:").toByteArray())
                        request.headers.add(HttpHeaders.AUTHORIZATION, "Basic $encodedSecretKey")
                        execution.execute(request, body)
                    },
                )
            },
        )
}

interface TossRestTemplate {
    val baseUrl: String
    val secretKey: String
    val restTemplate: RestTemplate

    fun confirmPayment(req: TossPaymentConfirmRequest): PaymentExecutionResult
}

class TossRestTemplateImpl(
    override val baseUrl: String,
    override val secretKey: String,
    override val restTemplate: RestTemplate,
) : TossRestTemplate {
    override fun confirmPayment(req: TossPaymentConfirmRequest): PaymentExecutionResult =
        try {
            sendPaymentConfirmationRequest(req)
        } catch (ex: PSPConfirmationException) {
            if (ex.isRetryableError) {
                retry(action = { sendPaymentConfirmationRequest(req) })
            } else {
                throw ex
            }
        }

    private fun sendPaymentConfirmationRequest(req: TossPaymentConfirmRequest): PaymentExecutionResult {
        try {
            val responseEntity =
                restTemplate.postForEntity(
                    "/v1/payments/confirm",
                    req,
                    TossPaymentConfirmationResponse::class.java,
                )

            val paymentResponse = requireNotNull(responseEntity.body) { "Response body is null" }
            return PaymentExecutionResult(
                orderId = paymentResponse.orderId,
                paymentKey = paymentResponse.paymentKey,
                extraDetails =
                    PaymentExtraDetails(
                        type = PaymentType.get(paymentResponse.type),
                        method = PaymentMethod.get(paymentResponse.method),
                        approvedAt = LocalDateTime.parse(paymentResponse.approvedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        pspRawData = paymentResponse.toString(),
                        orderName = paymentResponse.orderName,
                        pspConfirmationStatus = PSPConfirmationStatus.get(paymentResponse.status),
                        totalAmount = paymentResponse.totalAmount.toLong(),
                    ),
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false,
            )
        } catch (ex: HttpStatusCodeException) {
            val objectMapper = ObjectMapper()
            val failureResponse = objectMapper.readValue(ex.responseBodyAsString, TossFailureResponse::class.java)
            val paymentError = TossPaymentError.get(failureResponse.code)

            throw PSPConfirmationException(
                errorCode = paymentError.name,
                errorMessage = paymentError.description,
                isSuccess = paymentError.isSuccess(),
                isFailure = paymentError.isFailure(),
                isUnknown = paymentError.isUnknown(),
                isRetryableError = paymentError.isRetryableError(),
            )
        }
    }

    private fun retry(
        action: () -> PaymentExecutionResult,
        maxRetries: Int = 2,
    ): PaymentExecutionResult {
        val retryDelays = listOf(1000L, 2000L, 3000L) // 필요시 재시도 대기 시간 추가
        var lastException: PSPConfirmationException? = null

        // 첫 번째 시도는 이미 실패했으므로, 추가 재시도부터 카운트
        for (attempt in 0 until maxRetries) {
            try {
                return action()
            } catch (ex: PSPConfirmationException) {
                if (!ex.isRetryableError) throw ex
                lastException = ex

                // 대기 시간 선택 (배열 범위 초과 방지)
                val currentRetryDelay = if (attempt < retryDelays.size) retryDelays[attempt] else retryDelays.last()
                Thread.sleep(currentRetryDelay)
            }
        }

        throw PSPConfirmationException(
            errorCode = lastException?.errorCode ?: "UNKNOWN ERROR CODE",
            errorMessage = lastException?.errorMessage ?: "UNKNOWN ERROR MESSAGE",
            isSuccess = false,
            isFailure = false,
            isUnknown = true,
            isRetryableError = false,
        )
    }
}
