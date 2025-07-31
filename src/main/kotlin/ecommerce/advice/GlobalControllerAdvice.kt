package ecommerce.advice

import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.exception.ErrorResponse
import ecommerce.exception.NotFoundException
import ecommerce.exception.ProductValidationException
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(e: NotFoundException): ErrorResponse {
        return ErrorResponse(
            error = "NOT_FOUND",
            message = e.message ?: "Resource not found",
        )
    }

    @ExceptionHandler(ProductValidationException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleProductValidationException(e: ProductValidationException): ErrorResponse {
        return ErrorResponse(
            error = "VALIDATION_ERROR",
            message = e.message ?: "Validation failed",
            fieldErrors = null,
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleAuthenticationException(e: AuthenticationException): ErrorResponse {
        return ErrorResponse(
            error = "Unauthorized",
            message = e.message ?: "Authentication failed",
        )
    }

    @ExceptionHandler(AuthorizationException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleAuthorizationException(e: AuthorizationException): ErrorResponse {
        return ErrorResponse(
            error = "Forbidden",
            message = e.message ?: "Access denied",
        )
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(
            error = "BAD_REQUEST",
            message = e.message ?: "Invalid request",
        )
    }
}
