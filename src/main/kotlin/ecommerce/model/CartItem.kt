package ecommerce.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cart_items")
class CartItem(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    var cart: Cart,
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_option_id", nullable = true)
    var productOption: ProductOption,
    @Column(name = "quantity", nullable = false)
    var quantity: Int,
    @Column(name = "updatedAt", nullable = false)
    var itemAddedAt: LocalDateTime? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    fun modify(
        cart: Cart?,
        productOption: ProductOption?,
        quantity: Int,
        itemAddedAt: LocalDateTime?,
    ) {
        if (cart != null) {
            this.cart = cart
        }
        if (productOption != null) {
            this.productOption = productOption
        }
        if (quantity != null) {
            this.quantity = quantity
        }
        if (itemAddedAt != null) {
            this.itemAddedAt = itemAddedAt
        }
    }

    override fun toString(): String {
        return "Cart Item(id=$id, productOption=$productOption, quantity=$quantity)"
    }
}
