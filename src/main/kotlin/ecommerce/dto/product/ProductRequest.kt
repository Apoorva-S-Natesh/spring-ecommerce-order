package ecommerce.dto.product

import jakarta.validation.constraints.*

data class ProductRequest(
    @field:NotNull(message = "Name must not be blank")
    @field:Size(max = 15, message = "Name must be at most 15 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]{1,100}$",
        message =
            "Name must be 1–15 characters and only include letters, digits, spaces, " +
                "and allowed special characters:( ), [ ], +, -, &, /, _",
    )
    val name: String,
    @field:NotNull(message = "Price must not be null")
    @field:Min(1, message = "Price must be greater than 0")
    val price: Double,
    @field:NotNull(message = "quantity must not be null")
    @field:Min(1, message = "quantity must be greater than 0")
    @field:Max(99999999, message = "quantity must be lesser than 100,000,000")
    val quantity: Int,
//    @field:NotEmpty
//    @field:Min(1, message = "At least one product option must exist")
//    val productOptions: List<ProductOption>,
    @field:NotNull(message = "Image Link must not be null")
    @field:Pattern(
        regexp = "^(http://|https://).*",
        message = "url must begin with http:// or https://",
    )
    val imageUrl: String,
)
