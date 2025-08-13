package ecommerce.interceptor

import ecommerce.dto.auth.AuthenticatedUser
import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.model.Role
import ecommerce.service.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthInterceptor(private val tokenService: TokenService) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        logger.debug("Processing request: ${request.requestURI}, Authorization: ${request.getHeader(AUTH_HEADER)}")
        val token =
            extractToken(request) ?: run {
                logger.warn("Missing or invalid Authorization header")
                throw AuthenticationException("Missing or invalid Authorization header")
            }

        val claims =
            tokenService.validateToken(token) ?: run {
                logger.warn("Invalid or expired token: $token")
                throw AuthenticationException("Invalid or expired token")
            }

        val userId =
            extractUserId(claims) ?: run {
                logger.warn("Invalid token payload: missing userId")
                throw AuthenticationException("Invalid token payload")
            }

        logger.debug("Authenticated userId: $userId")
        storeAuthenticatedUser(request, claims, userId)

        if (requiresAdminAccess(request) && !hasAdminRole(claims)) {
            logger.warn("Admin access required for ${request.requestURI}")
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
                role = Role.valueOf(claims["role"] as? String ?: "USER"),
                email = claims["email"] as? String ?: "",
                name = claims["name"] as? String ?: "",
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
        private const val ADMIN_PATH_PREFIX = "/api/admin/"
        private val logger = LoggerFactory.getLogger(AuthInterceptor::class.java)
    }
}
