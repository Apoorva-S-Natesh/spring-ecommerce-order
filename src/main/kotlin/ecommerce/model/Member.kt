package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table

@Entity
@Table(name = "members")
class Member(
    @Column(name = "email", nullable = false, unique = true)
    val email: String,
    @Column(name = "password", nullable = false)
    val password: String,
    @Column(name = "name", nullable = false)
    val name: String,
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER,
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = true)
    var cart: Cart? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) {
    constructor(
        email: String,
        name: String,
    ) : this(
        email = email,
        password = "",
        name = name,
    )

    override fun toString(): String {
        return "Member(id=$id, email=$email, name=$name, role=$role, cart=$cart)"
    }
}
