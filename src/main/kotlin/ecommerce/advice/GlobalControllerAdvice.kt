package ecommerce.advice

import ecommerce.exception.AuthenticationException
import ecommerce.exception.AuthorizationException
import ecommerce.exception.DuplicateNameException
import ecommerce.exception.ErrorResponse
import ecommerce.exception.InsufficientProductOptionsException
import ecommerce.exception.NotFoundException
import ecommerce.exception.OrderProcessingException
import ecommerce.exception.ProductValidationException
import ecommerce.exception.StripePaymentException
import ecommerce.stripe.DeclineCode
import ecommerce.stripe.StripeApiError
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalControllerAdvice {
    private val log = LoggerFactory.getLogger(javaClass)

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
        log.error("Authentication failed: {}", e.message)
        return createErrorResponse(e, e.errorCode, HttpStatus.UNAUTHORIZED)
    }

    @ExceptionHandler(AuthorizationException::class)
    fun handleAuthorizationException(e: AuthorizationException): ResponseEntity<ErrorResponse> {
        log.error("Authorization failed: {}", e.message)
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

    @ExceptionHandler(StripePaymentException::class)
    fun handleStripePaymentException(e: StripePaymentException): ResponseEntity<ErrorResponse> {
        val (errorCode, status, message) =
            when (val code = e.declineCode) {
                in DeclineCode.entries.map { it.stripeCode } -> {
                    val decline = DeclineCode.fromStripeCode(code)
                    Triple(decline.stripeCode, HttpStatus.BAD_REQUEST, decline.userMessage)
                }
                "authentication_required", "invalid_request_error", "rate_limit", "api_connection_error" -> {
                    val apiError = StripeApiError.fromStripeCode(code)
                    Triple(apiError.stripeCode, apiError.httpStatus, apiError.userMessage)
                }
                else -> {
                    Triple(
                        "STRIPE_ERROR",
                        HttpStatus.BAD_REQUEST,
                        e.message ?: "An error occurred during payment processing.",
                    )
                }
            }
        return createErrorResponse(e, errorCode, status, listOf(message))
    }

    @ExceptionHandler(OrderProcessingException::class)
    fun handleOrderProcessingException(e: OrderProcessingException): ResponseEntity<ErrorResponse> {
        return createErrorResponse(e, "BAD_REQUEST", HttpStatus.BAD_REQUEST)
    }
}
