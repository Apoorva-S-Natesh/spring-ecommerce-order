package ecommerce.dto.auth

import ecommerce.model.Role

class AuthenticatedUser(
    val userId: Long,
    val role: Role,
    val email: String,
    val name: String,
) {
    fun isAdmin(): Boolean = role == Role.ADMIN

    fun isUser(): Boolean = role == Role.USER
}
