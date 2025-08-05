package ecommerce.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "carts")
class Cart(
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id", nullable = true)
    val member: Member? = null,
    @OneToMany(mappedBy = "cart", cascade = [CascadeType.ALL], fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    val cartItem: MutableList<CartItem> = mutableListOf(),
    @Column(name = "quantity", nullable = false)
    var quantity: Int = 0,
    @Column(name = "updated_at", nullable = false)
    val newItemAddedAt: LocalDateTime = LocalDateTime.now(),
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    constructor() : this(
        member = null,
        cartItem = mutableListOf(),
        quantity = 0,
        newItemAddedAt = LocalDateTime.now(),
    )

    constructor(member: Member) : this(
        member = member,
        cartItem = mutableListOf(),
        quantity = 0,
        newItemAddedAt = LocalDateTime.now(),
    )

    override fun toString(): String {
        return "Cart(id=$id, member=$member, cartItem=$cartItem, quantity=$quantity)"
    }
}
