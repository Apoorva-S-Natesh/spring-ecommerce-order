package ecommerce.advice

import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.ErrorResponse
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import ecommerce.exception.ProductValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    private fun createErrorResponse(
        exception: Exception,
        errorCode: String,
        status: HttpStatus,
        fieldErrors: List<String>? = null,
    ): ResponseEntity<ErrorResponse> {
        val response =
            ErrorResponse(
                error = errorCode,
                message = exception.message!!,
                fieldErrors = fieldErrors,
            )
        return ResponseEntity(response, status)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: NotFoundException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(ProductValidationException::class)
    fun handleProductValidationException(e: ProductValidationException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.BAD_REQUEST, e.errors)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.FORBIDDEN)
    }

    @ExceptionHandler(DuplicateNameException::class)
    fun handleDuplicateNameException(e: DuplicateNameException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.CONFLICT)
    }

    @ExceptionHandler(InsufficientProductOptionsException::class)
    fun handleIllegalArgumentException(e: InsufficientProductOptionsException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, e.errorCode, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ErrorResponse {
        return ErrorResponse(
            error = "BAD_REQUEST",
            message = e.message!!,
        )
    }
}
