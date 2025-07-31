package ecommerce.controller

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.dto.cart.AddToCartRequest
import ecommerce.dto.cart.UpdateQuantityRequest
import ecommerce.model.Cart
import ecommerce.service.CartService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RequestMapping("/api")
@RestController
class CartController(
    private val cartService: CartService,
) {
    @GetMapping("/cart-items")
    fun getCartItems(user: AuthenticatedUser): List<Cart> {
        return cartService.getCartItems(user.userId)
    }

    @PostMapping("/cart-items")
    fun addToCart(
        @Valid @RequestBody addToCartRequest: AddToCartRequest,
        user: AuthenticatedUser,
    ): ResponseEntity<Cart> {
        val cart = cartService.addToCart(user.userId, addToCartRequest)
        return ResponseEntity.created(URI.create("/api/cart-items")).body(cart)
    }

    @PutMapping("/cart-items/{productId}")
    fun updateQuantity(
        @PathVariable productId: Long,
        @Valid @RequestBody updateRequest: UpdateQuantityRequest,
        user: AuthenticatedUser,
    ): Cart {
        return cartService.updateQuantity(user.userId, productId, updateRequest)
    }

    @DeleteMapping("/cart-items/{productId}")
    fun removeFromCart(
        @PathVariable productId: Long,
        user: AuthenticatedUser,
    ): ResponseEntity<Unit> {
        cartService.removeFromCart(user.userId, productId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/cart-items")
    fun clearCart(user: AuthenticatedUser): ResponseEntity<Unit> {
        cartService.clearCart(user.userId)
        return ResponseEntity.noContent().build()
    }
}
