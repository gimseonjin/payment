package com.kerry.payment.payment.presentation.api.response

import org.springframework.http.HttpStatus

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