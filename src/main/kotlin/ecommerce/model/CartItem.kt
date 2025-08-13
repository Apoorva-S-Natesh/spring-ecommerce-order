package ecommerce.model

import ecommerce.dto.cart.AddToCartRequest
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.time.LocalDateTime
import java.util.Objects

@Entity
@Table(
    name = "cart_items",
    uniqueConstraints = [UniqueConstraint(columnNames = ["cart_id", "product_option_id"])],
)
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    var cart: Cart,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_option_id", nullable = false)
    var productOption: ProductOption,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "updatedAt", nullable = false)
    var itemAddedAt: LocalDateTime? = LocalDateTime.now(),
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

    fun update(
        request: AddToCartRequest,
        productOption: ProductOption,
        cart: Cart,
        isDirectUpdate: Boolean,
    ) {
        validateQuantity(request, productOption)
        if (isDirectUpdate) {
            cart.updateQuantity(request.newProductOptionQuantity)
        } else {
            cart.updateQuantity(request.newProductOptionQuantity)
            quantity += request.newProductOptionQuantity
        }
        itemAddedAt = LocalDateTime.now()
    }

    private fun validateQuantity(
        request: AddToCartRequest,
        productOption: ProductOption,
    ) {
        if (request.newProductOptionQuantity > productOption.quantity) {
            throw IllegalArgumentException(
                "Requested quantity (${request.newProductOptionQuantity}) " +
                    "exceeds available stock (${productOption.quantity})",
            )
        }
    }

    companion object {
        fun create(
            cart: Cart,
            productOption: ProductOption,
            request: AddToCartRequest,
        ): CartItem {
            productOption.validateQuantity(request.newProductOptionQuantity)
            return CartItem(
                cart = cart,
                productOption = productOption,
                quantity = request.newProductOptionQuantity,
                itemAddedAt = LocalDateTime.now(),
            )
        }
    }
}
