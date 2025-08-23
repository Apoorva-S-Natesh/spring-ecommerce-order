package ecommerce.stripe

import org.springframework.http.HttpStatus

enum class StripeApiError(val stripeCode: String, val userMessage: String, val httpStatus: HttpStatus) {
    AUTHENTICATION_REQUIRED(
        "authentication_required",
        "Authentication with the card issuer is required.",
        HttpStatus.UNAUTHORIZED,
    ),
    INVALID_REQUEST(
        "invalid_request_error",
        "Invalid request parameters.",
        HttpStatus.BAD_REQUEST,
    ),
    RATE_LIMIT(
        "rate_limit",
        "Too many requests. Please try again later.",
        HttpStatus.TOO_MANY_REQUESTS,
    ),
    API_CONNECTION_ERROR(
        "api_connection_error",
        "Unable to connect to Stripe. Please try again later.",
        HttpStatus.SERVICE_UNAVAILABLE,
    ),
    UNKNOWN("unknown", "An unexpected error occurred during payment processing.", HttpStatus.BAD_REQUEST),
    ;

    companion object {
        fun fromStripeCode(code: String?): StripeApiError {
            return entries.find { it.stripeCode == code } ?: UNKNOWN
        }
    }
}
