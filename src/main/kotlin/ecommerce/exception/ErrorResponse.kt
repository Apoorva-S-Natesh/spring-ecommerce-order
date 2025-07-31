package ecommerce.exception

data class ErrorResponse(
    val error: String,
    val message: String,
    val fieldErrors: Map<String, String>? = null,
)
