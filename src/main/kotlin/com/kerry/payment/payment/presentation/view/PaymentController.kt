package com.kerry.payment.payment.presentation.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class PaymentController {
    @GetMapping("/success")
    fun successPage(): String = "success"

    @GetMapping("/fail")
    fun failPage(): String = "fail"
}
