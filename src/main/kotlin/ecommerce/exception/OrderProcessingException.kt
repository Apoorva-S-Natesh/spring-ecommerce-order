package ecommerce.exception

open class OrderProcessingException(
    val declineCode: String,
    message: String = "Error in processing order",
    cause: Throwable? = null,
) : RuntimeException(message, cause)
