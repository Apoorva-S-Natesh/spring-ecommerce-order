package ecommerce.exception

class StripePaymentException(
    val declineCode: String? = null,
    override val message: String?,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
