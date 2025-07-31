package ecommerce.interceptor

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.model.Role
import ecommerce.service.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val tokenService: TokenService) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val token =
            extractToken(request)
                ?: throw AuthenticationException("Missing or invalid Authorization header")

        val claims =
            tokenService.validateToken(token)
                ?: throw AuthenticationException("Invalid or expired token")

        val userId =
            extractUserId(claims)
                ?: throw AuthenticationException("Invalid token payload")

        storeAuthenticatedUser(request, claims, userId)

        if (requiresAdminAccess(request) && !hasAdminRole(claims)) {
            throw AuthorizationException("Admin access required")
        }

        return true
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(AUTH_HEADER)
        return if (authHeader?.startsWith(BEARER_PREFIX) == true) {
            authHeader.substring(BEARER_PREFIX.length)
        } else {
            null
        }
    }

    private fun extractUserId(claims: io.jsonwebtoken.Claims): Long? {
        return claims.subject?.toLongOrNull()
    }

    private fun storeAuthenticatedUser(
        request: HttpServletRequest,
        claims: io.jsonwebtoken.Claims,
        userId: Long,
    ) {
        val authenticatedUser =
            AuthenticatedUser(
                userId = userId,
                role = claims["role"] as? String,
                email = claims["email"] as? String,
                name = claims["name"] as? String,
            )
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser)
    }

    private fun requiresAdminAccess(request: HttpServletRequest): Boolean {
        val uri = request.requestURI
        return uri == ADMIN_PATH || uri.startsWith(ADMIN_PATH_PREFIX)
    }

    private fun hasAdminRole(claims: io.jsonwebtoken.Claims): Boolean {
        return claims["role"] as? String == Role.ADMIN.name
    }

    companion object {
        const val AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser"
        const val AUTH_HEADER = "Authorization"
        private const val BEARER_PREFIX = "Bearer "
        private const val ADMIN_PATH = "/admin"
        private const val ADMIN_PATH_PREFIX = "$ADMIN_PATH/"
    }
}
