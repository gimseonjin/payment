package com.kerry.payment.view

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CheckoutController {

    @GetMapping("/")
    fun checkoutPage(): String{
        return "checkout"
    }

    @GetMapping("/billing")
    fun billingPage(): String{
        return "billing"
    }
}