package ecommerce.exception

class ProductValidationException(
    val errors: List<String>,
) : RuntimeException(errors.joinToString("; ")) {
    constructor(error: String) : this(listOf(error))
}
