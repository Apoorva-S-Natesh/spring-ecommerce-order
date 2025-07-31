package ecommerce.dto.cart

import jakarta.validation.constraints.Min

data class UpdateQuantityRequest(
    @field:Min(value = 1, message = "Quantity must be at least 1")
    val quantity: Int,
)
