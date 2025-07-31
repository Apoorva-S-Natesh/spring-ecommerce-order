package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.dto.cart.UpdateQuantityRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository,
) {
    fun getCartItems(userId: Long): List<Cart> {
        return cartRepository.findByUserId(userId)
    }

    fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): Cart {
        productRepository.findById(request.productId)

        val existingCart = cartRepository.findByUserIdAndProductId(userId, request.productId)

        return if (existingCart != null) {
            val updatedCart = existingCart.copy(quantity = existingCart.quantity + request.quantity)
            cartRepository.update(updatedCart)
        } else {
            val newCart =
                Cart(
                    memberId = userId,
                    productId = request.productId,
                    quantity = request.quantity,
                    addedAt = LocalDateTime.now(),
                )
            cartRepository.save(newCart)
        }
    }

    fun updateQuantity(
        userId: Long,
        productId: Long,
        request: UpdateQuantityRequest,
    ): Cart {
        productRepository.findById(productId)

        val existingCart =
            cartRepository.findByUserIdAndProductId(userId, productId)
                ?: throw NotFoundException("Item not found in cart")

        val updatedCart = existingCart.copy(quantity = request.quantity)
        return cartRepository.update(updatedCart)
    }

    fun removeFromCart(
        userId: Long,
        productId: Long,
    ) {
        cartRepository.deleteByUserIdAndProductId(userId, productId)
    }

    fun clearCart(userId: Long) {
        cartRepository.deleteByUserId(userId)
    }
}
