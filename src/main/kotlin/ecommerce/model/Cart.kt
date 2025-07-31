package ecommerce.model

import java.time.LocalDateTime

data class Cart(
    val id: Long? = null,
    val memberId: Long,
    val productId: Long,
    val quantity: Int,
    val addedAt: LocalDateTime? = null,
)
