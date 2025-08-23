package ecommerce.service

import ecommerce.dto.PaymentRequest
import ecommerce.dto.PaymentResponse
import ecommerce.exception.OrderProcessingException
import ecommerce.exception.StripePaymentException
import ecommerce.stripe.DeclineCode
import ecommerce.stripe.StripeClient
import org.springframework.stereotype.Service

@Service
class PaymentService(private val stripeClient: StripeClient) {
    fun processPayment(paymentRequest: PaymentRequest): PaymentResponse {
        try {
            return stripeClient.createCheckoutSession(paymentRequest)
        } catch (e: StripePaymentException) {
            val decline = DeclineCode.fromStripeCode(e.declineCode)
            throw OrderProcessingException(decline.stripeCode, decline.userMessage, e)
        }
    }
}
