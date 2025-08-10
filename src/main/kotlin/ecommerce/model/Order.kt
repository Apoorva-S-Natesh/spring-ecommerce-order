package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class MemberOrder (
    @OneToMany
    val orderItems: List<OrderItem> = listOf(),
    @Column
    val memberId: Long,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = true)
    val payment: Payment,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long?
)
