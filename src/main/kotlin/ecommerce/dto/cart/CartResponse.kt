package ecommerce.dto.cart

import ecommerce.dto.cartItem.CartItemResponse
import java.time.LocalDateTime

class CartResponse(
    val id: Long?,
    val quantity: Int,
    val newItemAddedAt: LocalDateTime,
    val memberId: Long?,
    val cartItems: List<CartItemResponse>? = null,
)
