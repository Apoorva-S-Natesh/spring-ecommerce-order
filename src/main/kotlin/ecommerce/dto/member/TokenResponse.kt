package ecommerce.dto.member

data class TokenResponse(
    val token: String,
    val type: String = "Bearer",
    val expiresIn: Long = 86400,
)
