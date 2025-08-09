package ecommerce.dto.member

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

class UpdateRequest(
    @field:Email(message = "Email format is invalid")
    @field:NotBlank(message = "Email must not be blank")
    val email: String,
    @field:NotBlank(message = "Name must not be blank")
    val name: String,
)
