package ecommerce.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime

@Entity
class Payment(
    @Column
    val checkoutSessionId: String? = null,
    @Column
    val amount: Long? = null,
    @Column
    @Enumerated(EnumType.STRING)
    val currency: Currency = Currency.UNKNOWN,
    @Column
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.UNKNOWN,
    @Column
    val paymentMethod: String? = null,
    @Column
    val createdAt: LocalDateTime? = LocalDateTime.now(),
    @Column
    val lastPaymentError: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
)
