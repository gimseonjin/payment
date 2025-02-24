package com.kerry.payment.payment.infra

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.DefaultUriBuilderFactory
import java.util.*

@Configuration
class TossWebClientConfiguration(
    @Value("\${PSP.toss.url}") private val baseUrl: String,
    @Value("\${PSP.toss.secret-key}") private val secretKey: String,
) {

    @Bean
    fun tossRestTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.uriTemplateHandler = DefaultUriBuilderFactory(baseUrl)

        val interceptor = ClientHttpRequestInterceptor { request, body, execution ->
            val encodedSecretKey = Base64.getEncoder().encodeToString(("$secretKey:").toByteArray())
            request.headers.add(HttpHeaders.AUTHORIZATION, "Basic $encodedSecretKey")
            execution.execute(request, body)
        }
        restTemplate.interceptors.add(interceptor)

        return restTemplate
    }
}