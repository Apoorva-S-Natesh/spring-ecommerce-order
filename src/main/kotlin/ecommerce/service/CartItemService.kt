package ecommerce.service

import ecommerce.dto.cart.AddToCartRequest
import ecommerce.exception.NotFoundException
import ecommerce.model.CartItem
import ecommerce.repository.CartItemRepository
import ecommerce.repository.CartRepository
import ecommerce.repository.ProductOptionRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import kotlin.jvm.optionals.getOrNull

@Service
class CartItemService(
    private val cartRepository: CartRepository,
    private val cartItemRepository: CartItemRepository,
    private val productOptionRepository: ProductOptionRepository,
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
        cartRepository.findById(cartId).getOrNull()
            ?: throw NotFoundException("Cart not found")

    private fun findProductOptionById(productOptionId: Long) =
        productOptionRepository.findById(productOptionId).getOrNull()
            ?: throw NotFoundException("Product option not found")

    private fun findExistingCartItem(
        cartItemId: Long?,
        cart: ecommerce.model.Cart,
        productOption: ecommerce.model.ProductOption,
    ) = cartItemId?.let { cartItemRepository.findById(it).getOrNull() }
        ?: cartItemRepository.findByCartAndProductOption(cart, productOption)

    private fun updateExistingCartItem(
        existingCartItem: CartItem,
        request: AddToCartRequest,
        productOption: ecommerce.model.ProductOption,
        cart: ecommerce.model.Cart,
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

        existingCartItem.modify(null, null, newQuantity, LocalDateTime.now())
        return cartItemRepository.save(existingCartItem)
    }

    private fun createNewCartItem(
        cart: ecommerce.model.Cart,
        productOption: ecommerce.model.ProductOption,
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
        productOption: ecommerce.model.ProductOption,
    ) {
        if (request.newProductOptionQuantity <= productOption.quantity) {
            throw IllegalArgumentException(
                "New product option quantity (${request.newProductOptionQuantity}) " +
                    "must be greater than current quantity (${productOption.quantity})",
            )
        }
    }

    private fun updateProductOptionQuantity(
        productOption: ecommerce.model.ProductOption,
        request: AddToCartRequest,
    ) {
        productOption.quantity = request.newProductOptionQuantity
        productOptionRepository.save(productOption)
    }

    private fun updateCartQuantity(
        cart: ecommerce.model.Cart,
        request: AddToCartRequest,
    ) {
        cart.quantity += request.newProductOptionQuantity
    }

    @Transactional
    fun deleteCartItemById(
        cartItemId: Long,
        cartId: Long,
    ) {
        cartRepository.findById(cartId).getOrNull()
            ?: throw NotFoundException("Cart not found")
        cartItemRepository.findById(cartItemId).getOrNull()
            ?: throw NotFoundException("Cart Item not found")
        cartItemRepository.deleteById(cartItemId)
    }

    @Transactional
    fun deleteAllCartItemsByCartId(cartId: Long) {
        cartRepository.findById(cartId).getOrNull()
            ?: throw NotFoundException("Cart not found")
        cartItemRepository.deleteAllByCartId(cartId)
    }
}
