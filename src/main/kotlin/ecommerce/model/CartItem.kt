package ecommerce.model

import ecommerce.dto.cartItem.CartItemResponse
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(name = "cart_items")
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    var cart: Cart,
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_option_id", nullable = true)
    var productOption: ProductOption,
    @Column(name = "quantity", nullable = true)
    var quantity: Int,
    @Column(name = "updatedAt", nullable = false)
    var itemAddedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CartItem) return false

        if (id == null || other.id == null) {
            return cart == other.cart && productOption == other.productOption
        }

        return id == other.id
    }

    override fun hashCode(): Int {
        return Objects.hash(cart.id, productOption.id)
    }

    fun toResponse() = CartItemResponse(id, cart.id, productOption.toResponse(), quantity)
}
