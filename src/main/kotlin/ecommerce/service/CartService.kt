package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.dto.cart.CartResponse
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductOptionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class CartService(
    private val cartRepository: CartRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val memberRepository: MemberRepository,
) {
    fun getCartByUserId(userId: Long): CartResponse {
        val cart =
            cartRepository.findByMemberId(userId)
                ?: throw NotFoundException("Cart not found for user $userId")
        return cart.toResponse()
    }

    @Transactional
    fun addToCart(
        userId: Long,
        request: AddToCartRequest,
    ): CartResponse {
        productOptionRepository.findById(request.productOptionId).getOrNull()
            ?: throw NotFoundException("Product option not found")
        val existingCart = cartRepository.findByMemberIdAndCartItemProductOptionId(userId, request.productOptionId)
        return if (existingCart != null) {
            val updatedCart =
                Cart(
                    member = existingCart.member,
                    cartItem = existingCart.cartItem,
                    quantity = existingCart.quantity + request.newProductOptionQuantity,
                    newItemAddedAt = LocalDateTime.now(),
                    id = existingCart.id,
                )
            cartRepository.save(updatedCart).toResponse()
        } else {
            val member =
                memberRepository.findById(userId).getOrNull()
                    ?: throw NotFoundException("Member not found")
            val newCart =
                Cart(
                    member = member,
                    cartItem = mutableListOf(),
                    quantity = request.newProductOptionQuantity,
                    newItemAddedAt = LocalDateTime.now(),
                )
            cartRepository.save(newCart).toResponse()
        }
    }

    @Transactional
    fun clearCart(userId: Long) {
        cartRepository.deleteByMemberId(userId)
    }

    @Transactional
    fun updateQuantity(
        userId: Long,
        productOptionId: Long,
        request: ecommerce.dto.cart.UpdateQuantityRequest,
    ): CartResponse {
        productOptionRepository.findById(productOptionId).getOrNull()
            ?: throw NotFoundException("Product option not found")

        val existingCart =
            cartRepository.findByMemberIdAndCartItemProductOptionId(userId, productOptionId)
                ?: throw NotFoundException("Item not found in cart")

        val updatedCart =
            Cart(
                member = existingCart.member,
                cartItem = existingCart.cartItem,
                quantity = request.quantity,
                newItemAddedAt = LocalDateTime.now(),
                id = existingCart.id,
            )
        return cartRepository.save(updatedCart).toResponse()
    }
}
