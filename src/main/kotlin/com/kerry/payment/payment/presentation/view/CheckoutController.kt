package com.kerry.payment.payment.presentation.view

import com.kerry.payment.common.IdempotencyCreator
import com.kerry.payment.payment.application.PaymentCheckoutCommand
import com.kerry.payment.payment.application.PaymentCheckoutService
import com.kerry.payment.payment.presentation.view.request.CheckoutRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CheckoutController(
    private val paymentCheckoutService: PaymentCheckoutService,
) {
    @GetMapping("/")
    fun checkoutPage(
        checkoutRequest: CheckoutRequest,
        model: Model,
    ): String {
        val checkoutResult =
            paymentCheckoutService.checkout(
                PaymentCheckoutCommand(
                    buyerId = checkoutRequest.buyerId,
                    productIds = checkoutRequest.productIds,
                    idempotencyKey = IdempotencyCreator.create(checkoutRequest.seed),
                ),
            )

        model.addAttribute("orderId", checkoutResult.orderId)
        model.addAttribute("orderName", checkoutResult.orderName)
        model.addAttribute("amount", checkoutResult.amount)

        return "checkout"
    }

    @GetMapping("/billing")
    fun billingPage(): String = "billing"
}
