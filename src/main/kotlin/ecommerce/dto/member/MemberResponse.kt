package ecommerce.dto.member

class MemberResponse(
    val id: Long?,
    val email: String,
    val name: String,
    val cartId: Long? = null,
)
