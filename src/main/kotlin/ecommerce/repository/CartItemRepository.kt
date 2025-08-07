package ecommerce.repository

import ecommerce.model.Cart
import ecommerce.model.CartItem
import ecommerce.model.ProductOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface CartItemRepository : JpaRepository<CartItem, Long> {
    fun findByCartAndProductOption(
        cart: Cart,
        productOption: ProductOption,
    ): CartItem?

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId")
    fun findByCartId(cartId: Long): List<CartItem>

    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    fun deleteAllByCartId(cartId: Long)
}
