package ecommerce.dto.auth

data class AuthenticatedUser(
    val userId: Long,
    val role: String?,
    val email: String?,
    val name: String?,
)
