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

    @ExceptionHandler(StripePaymentException::class)
    fun handleStripePaymentException(e: StripePaymentException): ResponseEntity<ErrorResponse> {
        val (errorCode, status, message) =
            when (e.declineCode) {
                in DeclineCode.entries.map { it.stripeCode } -> {
                    val decline = DeclineCode.fromStripeCode(e.declineCode)
                    Triple(decline.stripeCode, HttpStatus.BAD_REQUEST, decline.userMessage)
                }

                "authentication_required" ->
                    Triple(
                        "AUTHENTICATION_REQUIRED",
                        HttpStatus.UNAUTHORIZED,
                        "Authentication with the card issuer is required.",
                    )

                "invalid_request_error" ->
                    Triple(
                        "INVALID_REQUEST",
                        HttpStatus.BAD_REQUEST,
                        e.message ?: "Invalid request parameters.",
                    )

                "rate_limit" ->
                    Triple(
                        "RATE_LIMIT",
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Too many requests. Please try again later.",
                    )

                "api_connection_error" ->
                    Triple(
                        "API_CONNECTION_ERROR",
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "Unable to connect to Stripe. Please try again later.",
                    )

                else ->
                    Triple(
                        "STRIPE_ERROR",
                        HttpStatus.BAD_REQUEST,
                        e.message ?: "An error occurred during payment processing.",
                    )
            }
        return createErrorResponse(e, errorCode, status, listOf(message))
    }

    @ExceptionHandler(OrderProcessingException::class)
    fun handleOrderProcessingException(e: OrderProcessingException): ResponseEntity<ErrorResponse> {
        val status =
            when ("BAD_REQUEST") {
                in DeclineCode.entries.map { it.stripeCode } -> HttpStatus.BAD_REQUEST
                else -> HttpStatus.BAD_REQUEST
            }
        return createErrorResponse(e, "BAD_REQUEST", status)
    }
}
