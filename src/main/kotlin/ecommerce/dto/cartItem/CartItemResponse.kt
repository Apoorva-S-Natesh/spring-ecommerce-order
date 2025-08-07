package ecommerce.dto.cartItem

import ecommerce.dto.ProductOptionResponse

class CartItemResponse(
    val id: Long?,
    val cartId: Long?,
    val productOptionResponse: ProductOptionResponse,
    val quantity: Int,
)
