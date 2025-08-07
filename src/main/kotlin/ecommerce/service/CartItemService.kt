package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.exception.AuthorizationException
import ecommerce.exception.NotFoundException
import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.ProductOption
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.MemberRepository
import ecommerce.repository.ProductOptionRepository
import jakarta.transaction.Transactional
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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
    ): CartItem {
        val cart = findCartById(cartId)
        val productOption = findProductOptionById(request.productOptionId)
        val existingCartItem = findExistingCartItem(cartItemId, cart, productOption)

        return if (existingCartItem != null) {
            updateExistingCartItem(existingCartItem, request, productOption, cart, cartItemId != null)
        } else {
            createNewCartItem(cart, productOption, request)
        }
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

    private fun updateExistingCartItem(
        existingCartItem: CartItem,
        request: AddToCartRequest,
        productOption: ProductOption,
        cart: Cart,
        isDirectUpdate: Boolean,
    ): CartItem {
        if (isDirectUpdate) {
            validateQuantityIncrement(request, productOption)
            updateProductOptionQuantity(productOption, request)
            updateCartQuantity(cart, request)
        }

        val newQuantity =
            if (isDirectUpdate) {
                request.newProductOptionQuantity
            } else {
                existingCartItem.quantity + request.newProductOptionQuantity
            }
        existingCartItem.quantity = newQuantity
        existingCartItem.itemAddedAt = LocalDateTime.now()
        return cartItemRepository.save(existingCartItem)
    }

    private fun createNewCartItem(
        cart: Cart,
        productOption: ProductOption,
        request: AddToCartRequest,
    ) = cartItemRepository.save(
        CartItem(
            cart = cart,
            productOption = productOption,
            quantity = request.newProductOptionQuantity,
            itemAddedAt = LocalDateTime.now(),
        ),
    )

    private fun validateQuantityIncrement(
        request: AddToCartRequest,
        productOption: ProductOption,
    ) {
        if (request.newProductOptionQuantity <= productOption.quantity) {
            throw IllegalArgumentException(
                "New product option quantity (${request.newProductOptionQuantity}) " +
                    "must be greater than current quantity (${productOption.quantity})",
            )
        }
    }

    private fun updateProductOptionQuantity(
        productOption: ProductOption,
        request: AddToCartRequest,
    ) {
        productOption.quantity = request.newProductOptionQuantity
        productOptionRepository.save(productOption)
    }

    private fun updateCartQuantity(
        cart: Cart,
        request: AddToCartRequest,
    ) {
        cart.quantity += request.newProductOptionQuantity
    }

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
        cartItemRepository.deleteAllByCartId(cartId)
    }

    fun getCartItemsByCartId(
        cartId: Long,
        userId: Long,
    ): List<CartItem> {
        memberRepository.findByIdOrNull(userId) ?: throw AuthorizationException()
        cartRepository.findByIdOrNull(cartId) ?: throw NotFoundException("Cart requested not found")
        return cartItemRepository.findByCartId(cartId)
    }
}
