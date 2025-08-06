package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.service.CartItemService
import ecommerce.service.CartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/carts")
@RestController
class CartController(
    private val cartService: CartService,
    private val cartItemService: CartItemService,
) {
    @GetMapping("/{userId}")
    fun getCart(
        @PathVariable userId: Long,
    ): Cart {
        return cartService.getCartByUserId(userId)
    }

    @GetMapping("/{cartId}/items")
    fun getAllCartItemsOfCart(
        @PathVariable cartId: Long,
        user: AuthenticatedUser,
    ): List<CartItem> {
        return cartService.getCartItemsOfCartByCartId(cartId, user.userId)
    }

    @DeleteMapping("/{cartId}")
    fun deleteCart(
        @PathVariable cartId: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> {
        cartItemService.deleteAllCartItemsByCartId(cartId)
        return ResponseEntity.noContent().build()
    }
}
