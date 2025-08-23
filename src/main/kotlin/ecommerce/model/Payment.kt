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
    var checkoutSessionId: String? = null,
    @Column
    var amount: Long? = null,
    @Column
    @Enumerated(EnumType.STRING)
    var currency: Currency = Currency.UNKNOWN,
    @Column
    @Enumerated(EnumType.STRING)
    var status: PaymentStatus = PaymentStatus.UNKNOWN,
    @Column
    var paymentMethod: String? = null,
    @Column
    var createdAt: LocalDateTime? = LocalDateTime.now(),
    @Column
    var lastPaymentError: String? = null,
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
)
