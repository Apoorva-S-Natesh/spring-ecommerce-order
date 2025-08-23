package ecommerce.dto

import ecommerce.model.PaymentStatus
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime

class PlaceOrderRequest(
    @field:NotNull(message = "Option Id must not be null")
    val productOptionId: Long,
    @field:NotNull(message = "Quantity must not be null")
    @field:Min(1, message = "Quantity must be greater than 0")
    val quantity: Int,
    @field:NotBlank(message = "Currency must not be blank")
    val currency: String,
    @field:NotNull(message = "payment Method must not be null")
    @field:Pattern(regexp = "^pm_card_[a-z]+$", message = "Invalid payment method format")
    val paymentMethod: String,
)

class PaymentRequest(
    val amount: Long,
    val currency: String,
    val paymentMethod: String,
)

class PaymentResponse(
    val id: String,
    val amount: Long?,
    val status: String?,
    val paymentMethod: String?,
    val currency: String,
    val declineCode: String? = null,
)

class StripeErrorInfo(
    val message: String,
    val code: String? = null,
    val declineCode: String? = null,
)

class StripePaymentIntent(
    val id: String,
    val amount: Long,
    val currency: String,
    val status: String,
    val payment_method: String? = null,
    val last_payment_error: StripeLastPaymentError? = null,
)

class StripeLastPaymentError(
    val message: String? = null,
    val code: String? = null,
    val decline_code: String? = null,
)

class OrderItemResponse(
    val quantity: Int,
    val price: Double,
    val productOptionId: Long,
)

class OrderResponse(
    val id: Long?,
    val orderDate: LocalDateTime?,
    val status: PaymentStatus,
    val amount: Long?,
    val orderItems: List<OrderItemResponse>,
)
