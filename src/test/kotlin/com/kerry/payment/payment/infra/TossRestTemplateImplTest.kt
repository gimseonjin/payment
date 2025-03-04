package com.kerry.payment.payment.infra

import com.kerry.payment.payment.infra.response.PSPConfirmationException
import com.kerry.payment.payment.infra.response.TossPaymentError
import com.kerry.payment.payment.presentation.api.request.TossPaymentConfirmRequest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.util.*

class ErrorScenario(
    val errorCode: String,
    val isSuccess: Boolean,
    val isFailure: Boolean,
    val isUnknown: Boolean,
)

@SpringBootTest
@Tag("TooLongTimeTest")
class TossRestTemplateImplTest(
    @Value("\${PSP.toss.url}") private val baseUrl: String,
    @Value("\${PSP.toss.secret-key}") private val secretKey: String,
) {
    @Test
    fun `should handle correctly varius TossPaymentError scenarios`() {
        // given
        generateErrorScenarios().forEach {
            val testTossRestTemplate =
                TossRestTemplateImpl(
                    baseUrl,
                    secretKey,
                    RestTemplate().apply {
                        uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)
                        interceptors.add(
                            ClientHttpRequestInterceptor { request, body, execution ->
                                val encodedSecretKey = Base64.getEncoder().encodeToString(("$secretKey:").toByteArray())
                                request.headers.add(HttpHeaders.AUTHORIZATION, "Basic $encodedSecretKey")
                                request.headers.add("TossPayments-Test-Code", it.errorCode)
                                execution.execute(request, body)
                            },
                        )
                    },
                )

            // when
            try {
                testTossRestTemplate.confirmPayment(
                    TossPaymentConfirmRequest(
                        paymentKey = UUID.randomUUID().toString(),
                        orderId = UUID.randomUUID().toString(),
                        amount = 1000L,
                    ),
                )
            } catch (ex: PSPConfirmationException) {
                // then
                assertEquals(it.errorCode, ex.errorCode)
                assertEquals(it.isSuccess, ex.isSuccess)
                assertEquals(it.isFailure, ex.isFailure)
                assertEquals(it.isUnknown, ex.isUnknown)
            }
        }
    }

    private fun generateErrorScenarios(): List<ErrorScenario> =
        TossPaymentError.entries.map {
            ErrorScenario(
                errorCode = it.name,
                isSuccess = it.isSuccess(),
                isFailure = it.isFailure(),
                isUnknown = it.isUnknown(),
            )
        }
}
