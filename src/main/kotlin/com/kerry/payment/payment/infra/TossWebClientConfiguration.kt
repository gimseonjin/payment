@file:Suppress("ktlint:standard:no-wildcard-imports")

package com.kerry.payment.payment.infra

import com.kerry.payment.payment.domain.*
import com.kerry.payment.payment.infra.response.TossPaymentConfirmationResponse
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.http.client.ClientHttpRequestInterceptor
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
    fun tossRestTemplate(): TossRestTemplate = TossRestTemplateImpl(baseUrl, secretKey)
}

interface TossRestTemplate {
    val baseUrl: String
    val secretKey: String

    fun confirmPayment(req: TossPaymentConfirmRequest): PaymentExecutionResult
}

class TossRestTemplateImpl(
    override val baseUrl: String,
    override val secretKey: String,
) : RestTemplate(),
    TossRestTemplate {
    init {
        uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
        val interceptor =
            ClientHttpRequestInterceptor { request, body, execution ->
                val encodedSecretKey = Base64.getEncoder().encodeToString(("$secretKey:").toByteArray())
                request.headers.add(HttpHeaders.AUTHORIZATION, "Basic $encodedSecretKey")
                execution.execute(request, body)
            }
        interceptors.add(interceptor)
    }

    override fun confirmPayment(req: TossPaymentConfirmRequest): PaymentExecutionResult {
        val result: ResponseEntity<TossPaymentConfirmationResponse> =
            postForEntity(
                "/v1/payments/confirm",
                req,
                TossPaymentConfirmationResponse::class.java,
            )

        result.body?.let {
            return PaymentExecutionResult(
                orderId = it.orderId,
                paymentKey = it.paymentKey,
                extraDetails =
                    PaymentExtraDetails(
                        type = PaymentType.get(it.type),
                        method = PaymentMethod.get(it.method),
                        approvedAt = LocalDateTime.parse(it.approvedAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                        pspRawData = it.toString(),
                        orderName = it.orderName,
                        pspConfirmationStatus = PSPConfirmationStatus.get(it.status),
                        totalAmount = it.totalAmount.toLong(),
                    ),
                isSuccess = true,
                isFailure = false,
                isUnknown = false,
                isRetryable = false,
            )
        } ?: run {
            return PaymentExecutionResult(
                orderId = req.orderId,
                paymentKey = req.paymentKey,
                isSuccess = false,
                isFailure = true,
                isUnknown = false,
                isRetryable = true,
            )
        }
    }
}
