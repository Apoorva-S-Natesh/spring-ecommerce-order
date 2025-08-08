package ecommerce.model

import ecommerce.dto.cart.CartResponse
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "carts")
class Cart(
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = true)
    val member: Member? = null,
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    val cartItem: MutableList<CartItem> = mutableListOf(),
    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0,
    @Column(name = "updated_at", nullable = false)
    val newItemAddedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    constructor(member: Member) : this(
        member = member,
        cartItem = mutableListOf(),
        quantity = 0,
        newItemAddedAt = LocalDateTime.now(),
    )

    fun toResponse() =
        CartResponse(
            id = id ?: throw IllegalStateException("Cart ID cannot be null"),
            quantity = quantity,
            newItemAddedAt = newItemAddedAt,
            memberId = member?.id,
            cartItem.map { it.toResponse() },
        )

    fun updateQuantity(quantityIncrement: Int) {
        quantity += quantityIncrement
    }
}
