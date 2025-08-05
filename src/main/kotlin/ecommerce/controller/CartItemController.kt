package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.cart.AddToCartRequest
import ecommerce.model.CartItem
import ecommerce.service.CartItemService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RequestMapping("/api/cart/{cartId}/cart-items")
@RestController
class CartItemController(
    private val cartItemService: CartItemService,
) {
    @PostMapping("")
    fun addToCart(
        @PathVariable cartId: Long,
        @RequestBody request: AddToCartRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<CartItem> {
        val cartItem = cartItemService.saveCartItem(request, cartId)
        return ResponseEntity.ok(cartItem)
    }

    @PutMapping("/{itemId}")
    fun updateCartItemForIncrement(
        @PathVariable itemId: Long,
        @PathVariable cartId: Long,
        @RequestBody request: AddToCartRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<CartItem> {
        val updatedItem = cartItemService.saveCartItem(request, itemId, cartId)
        return ResponseEntity.ok(updatedItem)
    }

    @DeleteMapping("/{itemId}")
    fun deleteCartItem(
        @PathVariable itemId: Long,
        @PathVariable cartId: Long,
    ): ResponseEntity<Unit> {
        cartItemService.deleteCartItemById(itemId, cartId)
        return ResponseEntity.noContent().build()
    }
}
