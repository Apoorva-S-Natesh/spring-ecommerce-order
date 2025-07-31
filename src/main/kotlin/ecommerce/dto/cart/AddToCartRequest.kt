package ecommerce.dto.cart

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productId: Long,
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int = 1,
)
