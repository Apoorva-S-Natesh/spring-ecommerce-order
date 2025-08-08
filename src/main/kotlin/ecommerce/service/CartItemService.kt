package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.dto.cartItem.CartItemResponse
import ecommerce.exception.AuthorizationException
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductOptionRepository
import ecommerce.utils.ResponseMapper.cartItemToResponse
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class CartItemService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productOptionRepository: ProductOptionRepository,
    private val memberRepository: MemberRepository,
) {
    @Transactional
    fun saveCartItem(
        request: AddToCartRequest,
        cartId: Long,
        cartItemId: Long? = null,
    ): CartItemResponse {
        val cart = findCartById(cartId)
        val productOption = findProductOptionById(request.productOptionId)
        val existingCartItem = findExistingCartItem(cartItemId, cart, productOption)

        val cartItem =
            if (existingCartItem != null) {
                existingCartItem.update(request, productOption, cart, cartItemId != null)
                existingCartItem
            } else {
                CartItem.create(cart, productOption, request)
            }
        return cartItemToResponse(cartItemRepository.save(cartItem))
    }

    private fun findCartById(cartId: Long) =
        cartRepository.findByIdOrNull(cartId)
            ?: throw NotFoundException("Cart not found")

    private fun findProductOptionById(productOptionId: Long) =
        productOptionRepository.findByIdOrNull(productOptionId)
            ?: throw NotFoundException("Product option not found")

    private fun findExistingCartItem(
        cartItemId: Long?,
        cart: Cart,
        productOption: ProductOption,
    ) = cartItemId?.let { cartItemRepository.findByIdOrNull(it) }
        ?: cartItemRepository.findByCartAndProductOption(cart, productOption)

    @Transactional
    fun deleteCartItemById(
        cartItemId: Long,
        cartId: Long,
    ) {
        cartRepository.findByIdOrNull(cartId)
            ?: throw NotFoundException("Cart not found")
        cartItemRepository.findByIdOrNull(cartItemId)
            ?: throw NotFoundException("Cart Item not found")
        cartItemRepository.deleteById(cartItemId)
    }

    @Transactional
    fun deleteAllCartItemsByCartId(cartId: Long) {
        cartRepository.findByIdOrNull(cartId)
            ?: throw NotFoundException("Cart not found")
        cartItemRepository.deleteByCartId(cartId)
    }

    fun getCartItemsByCartId(
        cartId: Long,
        userId: Long,
    ): List<CartItemResponse> {
        memberRepository.findByIdOrNull(userId) ?: throw AuthorizationException()
        cartRepository.findByIdOrNull(cartId) ?: throw NotFoundException("Cart requested not found")
        return cartItemRepository.findByCartId(cartId).map { cartItemToResponse(it) }
    }
}
