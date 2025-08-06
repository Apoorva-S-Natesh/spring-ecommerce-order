package ecommerce.exception

class ProductValidationException(
    val errors: List<String> = listOf("Validation failed"),
) : Exceptions("VALIDATION_ERROR", errors.joinToString("; ")) {
    constructor(error: String) : this(listOf(error))
}
