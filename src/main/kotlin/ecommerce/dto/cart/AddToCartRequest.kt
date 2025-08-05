package ecommerce.dto.cart

import jakarta.validation.constraints.NotNull

data class AddToCartRequest(
    @field:NotNull(message = "Product ID is required")
    val productOptionId: Long,
    @field:NotNull(message = "Quantity is required")
    val newProductOptionQuantity: Int,
    @field:NotNull(message = "Cart item is required")
    val cartItemId: Long,
    @field:NotNull(message = "Cart is required")
    val cartId: Long,
)
