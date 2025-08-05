package ecommerce.dto.product

import ecommerce.model.Product
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class ProductOptionRequest(
    @field:NotNull(message = "Option Name must not be blank")
    @field:Size(max = 50, message = "Name must be at most 50 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 ()\\[\\]+\\-&/_]{1,100}$",
        message =
            "Name must be 1–50 characters and only include letters, digits, spaces, " +
                "and allowed special characters:( ), [ ], +, -, &, /, _",
    )
    val name: String,
    @field:NotNull(message = "quantity must not be null")
    @field:Min(1, message = "quantity must be greater than 0")
    @field:Max(99999999, message = "quantity must be lesser than 100,000,000")
    val quantity: Int,
    @field:NotNull(message = "Product must not be blank")
    val product: Product,
)
