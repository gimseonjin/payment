package com.kerry.payment.view

import com.kerry.payment.application.CheckoutCommand
import com.kerry.payment.application.CheckoutResult
import com.kerry.payment.application.CheckoutService
import com.kerry.payment.common.IdempotencyCreator
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

data class CheckoutRequest(
    val productIds: List<Long>,
    var buyerId: Long,
    var seed: String,
)

@Controller
class CheckoutController(
    private val checkoutService: CheckoutService
) {

    @GetMapping("/")
    fun checkoutPage(
        checkoutRequest: CheckoutRequest,
        model: Model
    ): String{
        val checkoutResult = checkoutService.checkout(CheckoutCommand(
            buyerId = checkoutRequest.buyerId,
            productIds = checkoutRequest.productIds,
            idempotencyKey = IdempotencyCreator.create(checkoutRequest.seed)
        ))

        model.addAttribute("orderId", checkoutResult.orderId)
        model.addAttribute("orderName", checkoutResult.orderName)
        model.addAttribute("amount", checkoutResult.amount)

        return "checkout"
    }

    @GetMapping("/billing")
    fun billingPage(): String{
        return "billing"
    }
}