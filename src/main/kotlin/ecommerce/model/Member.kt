package ecommerce.model

data class Member(
    val id: Long? = null,
    val email: String,
    val password: String,
    val name: String,
    val role: Role = Role.USER,
)
